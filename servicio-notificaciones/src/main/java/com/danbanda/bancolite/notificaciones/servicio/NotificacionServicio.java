package com.danbanda.bancolite.notificaciones.servicio;

import com.danbanda.bancolite.notificaciones.dto.EventoTransferencia;
import com.danbanda.bancolite.notificaciones.modelo.Alerta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionServicio {

    private static final Logger log = LoggerFactory.getLogger(NotificacionServicio.class);

    private final DetectorFraude detectorFraude;

    public NotificacionServicio(DetectorFraude detectorFraude) {
        this.detectorFraude = detectorFraude;
    }

    // por ahora la notificacion es un log, despues se puede cambiar por correo o push
    public void procesar(EventoTransferencia evento) {
        switch (evento.estado()) {
            case "COMPLETADA" -> {
                log.info("Notificacion a {}: enviaste {} MXN a la cuenta {}",
                        evento.cuentaOrigen(), evento.monto(), evento.cuentaDestino());
                log.info("Notificacion a {}: recibiste {} MXN de la cuenta {}",
                        evento.cuentaDestino(), evento.monto(), evento.cuentaOrigen());

                List<Alerta> alertas = detectorFraude.evaluar(evento);
                alertas.forEach(alerta -> log.warn("ALERTA {} en la cuenta {}: {}",
                        alerta.tipo(), alerta.cuenta(), alerta.descripcion()));
            }
            case "RECHAZADA" -> log.info("Notificacion a {}: tu transferencia de {} MXN fue rechazada. Motivo: {}",
                    evento.cuentaOrigen(), evento.monto(), evento.motivoFalla());
            default -> log.warn("Transferencia {} quedo en estado {}, se avisa a soporte",
                    evento.idTransferencia(), evento.estado());
        }
    }
}
