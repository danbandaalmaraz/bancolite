package com.danbanda.bancolite.transferencias.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transferencias")
public class Transferencia {

    @Id
    @Column(length = 36)
    private String id;

    // si el cliente manda la misma clave dos veces, no se cobra dos veces
    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 64)
    private String claveIdempotencia;

    // guardo solo el numero, la cuenta vive en otro microservicio y otra base
    @Column(name = "cuenta_origen", nullable = false, length = 10)
    private String cuentaOrigen;

    @Column(name = "cuenta_destino", nullable = false, length = 10)
    private String cuentaDestino;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(length = 140)
    private String concepto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTransferencia estado;

    @Column(name = "motivo_falla", length = 255)
    private String motivoFalla;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    protected Transferencia() {
    }

    public Transferencia(String claveIdempotencia, String cuentaOrigen, String cuentaDestino,
                         BigDecimal monto, String concepto) {
        this.id = UUID.randomUUID().toString();
        this.claveIdempotencia = claveIdempotencia;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.concepto = concepto;
        this.estado = EstadoTransferencia.PENDIENTE;
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    public void completar() {
        cambiarEstado(EstadoTransferencia.COMPLETADA, null);
    }

    public void rechazar(String motivo) {
        cambiarEstado(EstadoTransferencia.RECHAZADA, motivo);
    }

    public void revertir(String motivo) {
        cambiarEstado(EstadoTransferencia.REVERTIDA, motivo);
    }

    public void marcarFallida(String motivo) {
        cambiarEstado(EstadoTransferencia.FALLIDA, motivo);
    }

    private void cambiarEstado(EstadoTransferencia nuevoEstado, String motivo) {
        this.estado = nuevoEstado;
        this.motivoFalla = motivo;
        this.fechaActualizacion = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public String getCuentaOrigen() {
        return cuentaOrigen;
    }

    public String getCuentaDestino() {
        return cuentaDestino;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public EstadoTransferencia getEstado() {
        return estado;
    }

    public String getMotivoFalla() {
        return motivoFalla;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }
}
