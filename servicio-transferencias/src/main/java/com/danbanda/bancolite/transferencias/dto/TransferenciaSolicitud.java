package com.danbanda.bancolite.transferencias.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferenciaSolicitud(
        @NotBlank(message = "La cuenta origen es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cuenta origen debe tener 10 digitos")
        String cuentaOrigen,

        @NotBlank(message = "La cuenta destino es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cuenta destino debe tener 10 digitos")
        String cuentaDestino,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        @Digits(integer = 17, fraction = 2)
        BigDecimal monto,

        @Size(max = 140, message = "El concepto no puede pasar de 140 caracteres")
        String concepto
) {
}
