package com.danbanda.bancolite.cuentas.modelo;

import com.danbanda.bancolite.cuentas.excepcion.FondosInsuficientesExcepcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaTest {

    private Cuenta cuentaConSaldo(String saldo) {
        return new Cuenta("1234567890", "Dan Banda", "MXN", new BigDecimal(saldo));
    }

    @Test
    @DisplayName("El cargo resta del saldo")
    void cargoRestaSaldo() {
        Cuenta cuenta = cuentaConSaldo("1000.00");

        cuenta.cargar(new BigDecimal("300.50"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("699.50");
    }

    @Test
    @DisplayName("Se puede cargar el saldo exacto y la cuenta queda en cero")
    void cargoPorElSaldoExacto() {
        Cuenta cuenta = cuentaConSaldo("500.00");

        cuenta.cargar(new BigDecimal("500.00"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Un cargo mayor al saldo lanza excepcion y el saldo no cambia")
    void cargoSinFondos() {
        Cuenta cuenta = cuentaConSaldo("100.00");

        assertThatThrownBy(() -> cuenta.cargar(new BigDecimal("100.01")))
                .isInstanceOf(FondosInsuficientesExcepcion.class)
                .hasMessage("Fondos insuficientes en la cuenta 1234567890");

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("El abono suma al saldo")
    void abonoSumaSaldo() {
        Cuenta cuenta = cuentaConSaldo("100.00");

        cuenta.abonar(new BigDecimal("50.25"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("150.25");
    }

    @Test
    @DisplayName("Con BigDecimal no se pierden centavos")
    void noSePierdenCentavos() {
        Cuenta cuenta = cuentaConSaldo("0.00");

        cuenta.abonar(new BigDecimal("0.10"));
        cuenta.abonar(new BigDecimal("0.20"));

        // con double esto daria 0.30000000000000004
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("0.30");
    }
}
