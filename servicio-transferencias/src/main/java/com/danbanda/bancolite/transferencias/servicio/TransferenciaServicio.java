package com.danbanda.bancolite.transferencias.servicio;

import com.danbanda.bancolite.transferencias.cliente.ClienteCuentas;
import com.danbanda.bancolite.transferencias.cliente.CuentasNoDisponiblesExcepcion;
import com.danbanda.bancolite.transferencias.cliente.OperacionCuentaExcepcion;
import com.danbanda.bancolite.transferencias.evento.PublicadorEventos;
import com.danbanda.bancolite.transferencias.excepcion.TransferenciaInvalidaExcepcion;
import com.danbanda.bancolite.transferencias.excepcion.TransferenciaNoEncontradaExcepcion;
import com.danbanda.bancolite.transferencias.modelo.Transferencia;
import com.danbanda.bancolite.transferencias.repositorio.TransferenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransferenciaServicio {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaServicio.class);

    private final TransferenciaRepository transferenciaRepository;
    private final ClienteCuentas clienteCuentas;
    private final PublicadorEventos publicadorEventos;

    public TransferenciaServicio(TransferenciaRepository transferenciaRepository, ClienteCuentas clienteCuentas, PublicadorEventos publicadorEventos) {
        this.transferenciaRepository = transferenciaRepository;
        this.clienteCuentas = clienteCuentas;
        this.publicadorEventos = publicadorEventos;
    }

    // sin @Transactional a proposito: hay llamadas HTTP en medio y un rollback no las deshace
    public ResultadoTransferencia transferir(String claveIdempotencia, String cuentaOrigen,
                                             String cuentaDestino, BigDecimal monto, String concepto) {

        // 1. si esta clave ya se uso, regreso la transferencia que ya existe
        Optional<Transferencia> existente = transferenciaRepository.findByClaveIdempotencia(claveIdempotencia);
        if (existente.isPresent()) {
            return new ResultadoTransferencia(existente.get(), true);
        }

        if (cuentaOrigen.equals(cuentaDestino)) {
            throw new TransferenciaInvalidaExcepcion("La cuenta origen y destino no pueden ser la misma");
        }

        // 2. la registro como PENDIENTE antes de mover dinero, asi siempre queda rastro
        Transferencia transferencia = transferenciaRepository.save(
                new Transferencia(claveIdempotencia, cuentaOrigen, cuentaDestino, monto, concepto));

        // 3. valido que exista el destino y hago el cargo al origen
        try {
            clienteCuentas.validarQueExiste(cuentaDestino);
            clienteCuentas.cargar(cuentaOrigen, monto);
        } catch (OperacionCuentaExcepcion e) {
            transferencia.rechazar(e.getMessage());
            return guardar(transferencia);
        } catch (CuentasNoDisponiblesExcepcion e) {
            // no se sabe si el cargo alcanzo a aplicarse, se revisa a mano
            log.error("Cuentas no respondio en la transferencia {}", transferencia.getId(), e);
            transferencia.marcarFallida("Servicio de cuentas no disponible");
            return guardar(transferencia);
        }

        // 4. abono al destino, si falla le regreso el dinero al origen
        try {
            clienteCuentas.abonar(cuentaDestino, monto);
        } catch (RuntimeException e) {
            revertirCargo(transferencia, e);
            return guardar(transferencia);
        }

        transferencia.completar();
        return guardar(transferencia);
    }

    public Transferencia buscarPorId(String id) {
        return transferenciaRepository.findById(id)
                .orElseThrow(() -> new TransferenciaNoEncontradaExcepcion(id));
    }

    public List<Transferencia> movimientosDeCuenta(String numeroCuenta) {
        return transferenciaRepository
                .findTop20ByCuentaOrigenOrCuentaDestinoOrderByFechaCreacionDesc(numeroCuenta, numeroCuenta);
    }

    private void revertirCargo(Transferencia transferencia, RuntimeException causa) {
        try {
            clienteCuentas.abonar(transferencia.getCuentaOrigen(), transferencia.getMonto());
            transferencia.revertir("Fallo el abono a la cuenta destino: " + causa.getMessage());
        } catch (RuntimeException e) {
            log.error("No se pudo revertir el cargo de la transferencia {}", transferencia.getId(), e);
            transferencia.marcarFallida("Fallo el abono y tambien la reversa, requiere revision manual");
        }
    }

    private ResultadoTransferencia guardar(Transferencia transferencia) {
        Transferencia guardada = transferenciaRepository.save(transferencia);
        publicadorEventos.publicar(guardada);
        return new ResultadoTransferencia(guardada,false);
    }
}
