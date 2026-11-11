package com.danbanda.bancolite.transferencias.cliente;


public class OperacionCuentaExcepcion extends RuntimeException {

    private final int estatusHttp;

    public OperacionCuentaExcepcion(String mensaje, int estatusHttp) {
        super(mensaje);
        this.estatusHttp = estatusHttp;
    }

    public int getEstatusHttp() {
        return estatusHttp;
    }
}
