package com.danbanda.bancolite.transferencias.servicio;

import com.danbanda.bancolite.transferencias.cliente.ClienteCuentas;
import com.danbanda.bancolite.transferencias.cliente.CuentasNoDisponiblesExcepcion;
import com.danbanda.bancolite.transferencias.cliente.OperacionCuentaExcepcion;
import com.danbanda.bancolite.transferencias.evento.PublicadorEventos;
import com.danbanda.bancolite.transferencias.excepcion.TransferenciaInvalidaExcepcion;
import com.danbanda.bancolite.transferencias.modelo.EstadoTransferencia;
import com.danbanda.bancolite.transferencias.modelo.Transferencia;
import com.danbanda.bancolite.transferencias.repositorio.TransferenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferenciaServicioTest {

    private static final String ORIGEN = "1111111111";
    private static final String DESTINO = "2222222222";
    private static final BigDecimal MONTO = new BigDecimal("100.00");

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private ClienteCuentas clienteCuentas;

    @Mock
    private PublicadorEventos publicadorEventos;

    @InjectMocks
    private TransferenciaServicio transferenciaServicio;

    @BeforeEach
    void configurar() {
        // save() regresa la misma transferencia que recibe, como lo haria la base
        lenient().when(transferenciaRepository.save(any(Transferencia.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    @DisplayName("Si todo sale bien la transferencia queda COMPLETADA")
    void transferenciaExitosa() {
        ResultadoTransferencia resultado = transferenciaServicio.transferir("tx-1", ORIGEN, DESTINO, MONTO, "Renta");

        assertThat(resultado.repetida()).isFalse();
        assertThat(resultado.transferencia().getEstado()).isEqualTo(EstadoTransferencia.COMPLETADA);
        assertThat(resultado.transferencia().getMotivoFalla()).isNull();

        // primero se valida el destino, luego el cargo y al final el abono
        InOrder orden = inOrder(clienteCuentas);
        orden.verify(clienteCuentas).validarQueExiste(DESTINO);
        orden.verify(clienteCuentas).cargar(ORIGEN, MONTO);
        orden.verify(clienteCuentas).abonar(DESTINO, MONTO);

        // se guarda dos veces: en PENDIENTE y en COMPLETADA
        verify(transferenciaRepository, times(2)).save(any(Transferencia.class));
        verify(publicadorEventos).publicar(resultado.transferencia());
    }

    @Test
    @DisplayName("Si la clave de idempotencia ya existe no se mueve dinero otra vez")
    void claveRepetida() {
        Transferencia anterior = new Transferencia("tx-1", ORIGEN, DESTINO, MONTO, "Renta");
        anterior.completar();
        when(transferenciaRepository.findByClaveIdempotencia("tx-1")).thenReturn(Optional.of(anterior));

        ResultadoTransferencia resultado = transferenciaServicio.transferir("tx-1", ORIGEN, DESTINO, MONTO, "Renta");

        assertThat(resultado.repetida()).isTrue();
        assertThat(resultado.transferencia().getId()).isEqualTo(anterior.getId());
        verifyNoInteractions(clienteCuentas);
        verify(transferenciaRepository, never()).save(any());
        verifyNoInteractions(publicadorEventos);
    }

    @Test
    @DisplayName("No se puede transferir a la misma cuenta")
    void mismaCuenta() {
        assertThatThrownBy(() -> transferenciaServicio.transferir("tx-1", ORIGEN, ORIGEN, MONTO, null))
                .isInstanceOf(TransferenciaInvalidaExcepcion.class)
                .hasMessage("La cuenta origen y destino no pueden ser la misma");

        verifyNoInteractions(clienteCuentas);
    }

    @Test
    @DisplayName("Sin fondos la transferencia queda RECHAZADA y no se abona nada")
    void sinFondos() {
        doThrow(new OperacionCuentaExcepcion("Fondos insuficientes en la cuenta " + ORIGEN, 422))
                .when(clienteCuentas).cargar(ORIGEN, MONTO);

        ResultadoTransferencia resultado = transferenciaServicio.transferir("tx-1", ORIGEN, DESTINO, MONTO, null);

        assertThat(resultado.transferencia().getEstado()).isEqualTo(EstadoTransferencia.RECHAZADA);
        assertThat(resultado.transferencia().getMotivoFalla()).isEqualTo("Fondos insuficientes en la cuenta " + ORIGEN);
        verify(clienteCuentas, never()).abonar(any(), any());
    }

    @Test
    @DisplayName("Si cuentas no responde en el cargo la transferencia queda FALLIDA")
    void cuentasCaidoEnElCargo() {
        doThrow(new CuentasNoDisponiblesExcepcion("timeout", new RuntimeException()))
                .when(clienteCuentas).cargar(ORIGEN, MONTO);

        ResultadoTransferencia resultado = transferenciaServicio.transferir("tx-1", ORIGEN, DESTINO, MONTO, null);

        assertThat(resultado.transferencia().getEstado()).isEqualTo(EstadoTransferencia.FALLIDA);
        assertThat(resultado.transferencia().getMotivoFalla()).isEqualTo("Servicio de cuentas no disponible");
        verify(clienteCuentas, never()).abonar(any(), any());
    }

    @Test
    @DisplayName("Si falla el abono se le regresa el dinero al origen y queda REVERTIDA")
    void falloElAbonoSeRevierte() {
        doThrow(new CuentasNoDisponiblesExcepcion("timeout", new RuntimeException()))
                .when(clienteCuentas).abonar(DESTINO, MONTO);

        ResultadoTransferencia resultado = transferenciaServicio.transferir("tx-1", ORIGEN, DESTINO, MONTO, null);

        assertThat(resultado.transferencia().getEstado()).isEqualTo(EstadoTransferencia.REVERTIDA);
        verify(clienteCuentas).abonar(ORIGEN, MONTO); // la compensacion
    }

    @Test
    @DisplayName("Si falla el abono y tambien la reversa queda FALLIDA para revision manual")
    void falloElAbonoYLaReversa() {
        doThrow(new CuentasNoDisponiblesExcepcion("timeout", new RuntimeException()))
                .when(clienteCuentas).abonar(any(), any());

        ResultadoTransferencia resultado = transferenciaServicio.transferir("tx-1", ORIGEN, DESTINO, MONTO, null);

        assertThat(resultado.transferencia().getEstado()).isEqualTo(EstadoTransferencia.FALLIDA);
        assertThat(resultado.transferencia().getMotivoFalla()).contains("revision manual");
    }
}
