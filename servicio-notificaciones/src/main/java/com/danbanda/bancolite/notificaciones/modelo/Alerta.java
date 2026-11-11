package com.danbanda.bancolite.notificaciones.modelo;

import java.time.Instant;

public record Alerta(
        String idTransferencia,
        String cuenta,
        TipoAlerta tipo,
        String descripcion,
        Instant fechaDeteccion
) {
}
