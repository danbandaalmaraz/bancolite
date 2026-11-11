package com.danbanda.bancolite.cuentas.controlador;

import com.danbanda.bancolite.cuentas.dto.CuentaRespuesta;
import com.danbanda.bancolite.cuentas.dto.CrearCuentaSolicitud;
import com.danbanda.bancolite.cuentas.dto.MovimientoSolicitud;
import com.danbanda.bancolite.cuentas.modelo.Cuenta;
import com.danbanda.bancolite.cuentas.servicio.CuentaServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaServicio cuentaServicio;

    public CuentaController(CuentaServicio cuentaServicio) {
        this.cuentaServicio = cuentaServicio;
    }

    @PostMapping
    public ResponseEntity<CuentaRespuesta> crear(@Valid @RequestBody CrearCuentaSolicitud solicitud) {
        Cuenta cuenta = cuentaServicio.crear(solicitud.titular(), solicitud.saldoInicial());
        return ResponseEntity
                .created(URI.create("/api/cuentas/" + cuenta.getNumeroCuenta()))
                .body(CuentaRespuesta.desde(cuenta));
    }

    @GetMapping("/{numeroCuenta}")
    public CuentaRespuesta consultar(@PathVariable String numeroCuenta) {
        return CuentaRespuesta.desde(cuentaServicio.buscarPorNumero(numeroCuenta));
    }

    @PostMapping("/{numeroCuenta}/cargo")
    public CuentaRespuesta cargar(@PathVariable String numeroCuenta,
                                  @Valid @RequestBody MovimientoSolicitud solicitud) {
        return CuentaRespuesta.desde(cuentaServicio.cargar(numeroCuenta, solicitud.monto()));
    }

    @PostMapping("/{numeroCuenta}/abono")
    public CuentaRespuesta abonar(@PathVariable String numeroCuenta,
                                  @Valid @RequestBody MovimientoSolicitud solicitud) {
        return CuentaRespuesta.desde(cuentaServicio.abonar(numeroCuenta, solicitud.monto()));
    }
}
