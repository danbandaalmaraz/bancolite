package com.danbanda.bancolite.transferencias.evento;

import com.danbanda.bancolite.transferencias.modelo.EstadoTransferencia;
import com.danbanda.bancolite.transferencias.modelo.Transferencia;

import java.math.BigDecimal;
import java.time.Instant;

// lo que se publica en Pub/Sub, debe coincidir con lo que espera servicio-notificaciones
public record EventoTransferencia(
        String idTransferencia,
        String cuentaOrigen,
        String cuentaDestino,
        BigDecimal monto,
        EstadoTransferencia estado,
        String motivoFalla,
        Instant fecha
) {
    public static EventoTransferencia desde(Transferencia transferencia) {
        return new EventoTransferencia(
                transferencia.getId(),
                transferencia.getCuentaOrigen(),
                transferencia.getCuentaDestino(),
                transferencia.getMonto(),
                transferencia.getEstado(),
                transferencia.getMotivoFalla(),
                transferencia.getFechaActualizacion());
    }
}