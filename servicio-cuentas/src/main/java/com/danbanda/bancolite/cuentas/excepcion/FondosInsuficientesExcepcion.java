package com.danbanda.bancolite.cuentas.excepcion;

public class FondosInsuficientesExcepcion extends RuntimeException {

    public FondosInsuficientesExcepcion(String numeroCuenta) {
        super("Fondos insuficientes en la cuenta " + numeroCuenta);
    }
}
