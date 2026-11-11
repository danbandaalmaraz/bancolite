package com.danbanda.bancolite.transferencias.excepcion;

public class TransferenciaNoEncontradaExcepcion extends RuntimeException {

    public TransferenciaNoEncontradaExcepcion(String id) {
        super("No existe la transferencia " + id);
    }
}
