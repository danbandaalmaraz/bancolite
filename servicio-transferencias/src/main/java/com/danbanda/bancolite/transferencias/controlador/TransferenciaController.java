package com.danbanda.bancolite.transferencias.controlador;

import com.danbanda.bancolite.transferencias.dto.TransferenciaRespuesta;
import com.danbanda.bancolite.transferencias.dto.TransferenciaSolicitud;
import com.danbanda.bancolite.transferencias.excepcion.TransferenciaInvalidaExcepcion;
import com.danbanda.bancolite.transferencias.modelo.EstadoTransferencia;
import com.danbanda.bancolite.transferencias.servicio.ResultadoTransferencia;
import com.danbanda.bancolite.transferencias.servicio.TransferenciaServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransferenciaServicio transferenciaServicio;

    public TransferenciaController(TransferenciaServicio transferenciaServicio) {
        this.transferenciaServicio = transferenciaServicio;
    }

    @PostMapping
    public ResponseEntity<TransferenciaRespuesta> transferir(
            @RequestHeader("Idempotency-Key") String claveIdempotencia,
            @Valid @RequestBody TransferenciaSolicitud solicitud) {

        if (claveIdempotencia.isBlank() || claveIdempotencia.length() > 64) {
            throw new TransferenciaInvalidaExcepcion("El header Idempotency-Key debe tener entre 1 y 64 caracteres");
        }

        ResultadoTransferencia resultado = transferenciaServicio.transferir(
                claveIdempotencia,
                solicitud.cuentaOrigen(),
                solicitud.cuentaDestino(),
                solicitud.monto(),
                solicitud.concepto());

        TransferenciaRespuesta respuesta = TransferenciaRespuesta.desde(resultado.transferencia());

        // si es repetida regreso 200 con la misma transferencia, no se crea nada nuevo
        if (resultado.repetida()) {
            return ResponseEntity.ok(respuesta);
        }
        return ResponseEntity.status(estatusPara(respuesta.estado())).body(respuesta);
    }

    @GetMapping("/{id}")
    public TransferenciaRespuesta consultar(@PathVariable String id) {
        return TransferenciaRespuesta.desde(transferenciaServicio.buscarPorId(id));
    }

    // GET /api/transferencias?cuenta=8228692466
    @GetMapping
    public List<TransferenciaRespuesta> movimientos(@RequestParam("cuenta") String numeroCuenta) {
        return transferenciaServicio.movimientosDeCuenta(numeroCuenta).stream()
                .map(TransferenciaRespuesta::desde)
                .toList();
    }

    private HttpStatus estatusPara(EstadoTransferencia estado) {
        return switch (estado) {
            case COMPLETADA -> HttpStatus.CREATED;
            case RECHAZADA -> HttpStatus.UNPROCESSABLE_ENTITY;
            case REVERTIDA, FALLIDA -> HttpStatus.BAD_GATEWAY;
            case PENDIENTE -> HttpStatus.ACCEPTED;
        };
    }
}
