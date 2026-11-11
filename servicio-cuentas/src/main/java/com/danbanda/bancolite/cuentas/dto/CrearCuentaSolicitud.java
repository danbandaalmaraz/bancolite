package com.danbanda.bancolite.cuentas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CrearCuentaSolicitud(
        @NotBlank(message = "El titular es obligatorio")
        @Size(max = 120)
        String titular,

        @NotNull(message = "El saldo inicial es obligatorio")
        @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 17, fraction = 2)
        BigDecimal saldoInicial
) {
}
