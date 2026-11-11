package com.danbanda.bancolite.notificaciones.controlador;

import com.danbanda.bancolite.notificaciones.modelo.Alerta;
import com.danbanda.bancolite.notificaciones.servicio.DetectorFraude;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final DetectorFraude detectorFraude;

    public AlertaController(DetectorFraude detectorFraude) {
        this.detectorFraude = detectorFraude;
    }

    @GetMapping
    public List<Alerta> listar() {
        return detectorFraude.obtenerAlertas();
    }
}
