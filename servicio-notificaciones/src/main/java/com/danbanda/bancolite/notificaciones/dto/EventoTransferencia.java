package com.danbanda.bancolite.notificaciones.dto;

import java.math.BigDecimal;
import java.time.Instant;

// mismo formato que publica servicio-transferencias
public record EventoTransferencia(
        String idTransferencia,
        String cuentaOrigen,
        String cuentaDestino,
        BigDecimal monto,
        String estado,
        String motivoFalla,
        Instant fecha
) {
}
