package com.danbanda.bancolite.transferencias.dto;

import com.danbanda.bancolite.transferencias.modelo.EstadoTransferencia;
import com.danbanda.bancolite.transferencias.modelo.Transferencia;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferenciaRespuesta(
        String id,
        String cuentaOrigen,
        String cuentaDestino,
        BigDecimal monto,
        String concepto,
        EstadoTransferencia estado,
        String motivoFalla,
        Instant fechaCreacion
) {
    public static TransferenciaRespuesta desde(Transferencia transferencia) {
        return new TransferenciaRespuesta(
                transferencia.getId(),
                transferencia.getCuentaOrigen(),
                transferencia.getCuentaDestino(),
                transferencia.getMonto(),
                transferencia.getConcepto(),
                transferencia.getEstado(),
                transferencia.getMotivoFalla(),
                transferencia.getFechaCreacion());
    }
}
