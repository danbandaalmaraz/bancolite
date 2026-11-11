package com.danbanda.bancolite.cuentas.modelo;

import com.danbanda.bancolite.cuentas.excepcion.FondosInsuficientesExcepcion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 10)
    private String numeroCuenta;

    @Column(nullable = false, length = 120)
    private String titular;

    @Column(nullable = false, length = 3)
    private String moneda;

    // uso BigDecimal y no double porque con double se pierden centavos al redondear
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    // bloqueo optimista: si dos peticiones mueven la misma cuenta al mismo tiempo
    // la segunda falla en vez de pisar el saldo de la primera
    @Version
    private Long version;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    protected Cuenta() {
        // JPA necesita un constructor vacio
    }

    public Cuenta(String numeroCuenta, String titular, String moneda, BigDecimal saldoInicial) {
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.moneda = moneda;
        this.saldo = saldoInicial;
        this.fechaCreacion = Instant.now();
    }

    // la validacion del saldo la dejo aqui para que nadie pueda dejar la cuenta en negativo
    public void cargar(BigDecimal monto) {
        if (saldo.compareTo(monto) < 0) {
            throw new FondosInsuficientesExcepcion(numeroCuenta);
        }
        saldo = saldo.subtract(monto);
    }

    public void abonar(BigDecimal monto) {
        saldo = saldo.add(monto);
    }

    public Long getId() {
        return id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public String getTitular() {
        return titular;
    }

    public String getMoneda() {
        return moneda;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
