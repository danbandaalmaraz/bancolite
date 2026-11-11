package com.danbanda.bancolite.cuentas.servicio;

import com.danbanda.bancolite.cuentas.excepcion.CuentaNoEncontradaExcepcion;
import com.danbanda.bancolite.cuentas.modelo.Cuenta;
import com.danbanda.bancolite.cuentas.repositorio.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
public class CuentaServicio {

    private static final String MONEDA = "MXN";

    private final CuentaRepository cuentaRepository;
    private final SecureRandom aleatorio = new SecureRandom();

    public CuentaServicio(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Transactional
    public Cuenta crear(String titular, BigDecimal saldoInicial) {
        String numeroCuenta;
        do {
            numeroCuenta = generarNumeroCuenta();
        } while (cuentaRepository.existsByNumeroCuenta(numeroCuenta));

        return cuentaRepository.save(new Cuenta(numeroCuenta, titular, MONEDA, saldoInicial));
    }

    @Transactional(readOnly = true)
    public Cuenta buscarPorNumero(String numeroCuenta) {
        return obtenerCuenta(numeroCuenta);
    }

    // no llamo a save() porque la cuenta ya esta dentro de la transaccion,
    // JPA ve que cambio el saldo y hace el UPDATE solo
    @Transactional
    public Cuenta cargar(String numeroCuenta, BigDecimal monto) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        cuenta.cargar(monto);
        return cuenta;
    }

    @Transactional
    public Cuenta abonar(String numeroCuenta, BigDecimal monto) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        cuenta.abonar(monto);
        return cuenta;
    }

    private Cuenta obtenerCuenta(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaExcepcion(numeroCuenta));
    }

    // numero de 10 digitos
    private String generarNumeroCuenta() {
        return String.valueOf(aleatorio.nextLong(1_000_000_000L, 10_000_000_000L));
    }
}
