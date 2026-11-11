package com.danbanda.bancolite.cuentas.excepcion;

public class CuentaNoEncontradaExcepcion extends RuntimeException {

    public CuentaNoEncontradaExcepcion(String numeroCuenta) {
        super("No existe la cuenta " + numeroCuenta);
    }
}
