package com.danbanda.bancolite.transferencias.excepcion;

public class TransferenciaInvalidaExcepcion extends RuntimeException {

    public TransferenciaInvalidaExcepcion(String mensaje) {
        super(mensaje);
    }
}
