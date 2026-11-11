package com.danbanda.bancolite.cuentas.dto;

import com.danbanda.bancolite.cuentas.modelo.Cuenta;

import java.math.BigDecimal;
import java.time.Instant;

// lo que regresa la API, asi no expongo el id ni la version de la entidad
public record CuentaRespuesta(
        String numeroCuenta,
        String titular,
        String moneda,
        BigDecimal saldo,
        Instant fechaCreacion
) {
    public static CuentaRespuesta desde(Cuenta cuenta) {
        return new CuentaRespuesta(
                cuenta.getNumeroCuenta(),
                cuenta.getTitular(),
                cuenta.getMoneda(),
                cuenta.getSaldo(),
                cuenta.getFechaCreacion());
    }
}
