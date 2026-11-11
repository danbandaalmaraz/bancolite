package com.danbanda.bancolite.transferencias.cliente;

// cuentas esta caido, tardo demasiado o respondio 5xx
public class CuentasNoDisponiblesExcepcion extends RuntimeException {

    public CuentasNoDisponiblesExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
