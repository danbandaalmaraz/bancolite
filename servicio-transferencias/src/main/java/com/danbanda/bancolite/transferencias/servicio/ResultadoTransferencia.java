package com.danbanda.bancolite.transferencias.servicio;

import com.danbanda.bancolite.transferencias.modelo.Transferencia;

// repetida = true cuando la clave de idempotencia ya se habia usado
public record ResultadoTransferencia(Transferencia transferencia, boolean repetida) {
}