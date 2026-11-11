package com.danbanda.bancolite.notificaciones.controlador;

import com.danbanda.bancolite.notificaciones.dto.EventoTransferencia;
import com.danbanda.bancolite.notificaciones.dto.MensajePush;
import com.danbanda.bancolite.notificaciones.servicio.NotificacionServicio;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/pubsub")
public class PubSubController {

    private static final Logger log = LoggerFactory.getLogger(PubSubController.class);

    private final ObjectMapper objectMapper;
    private final NotificacionServicio notificacionServicio;

    // Pub/Sub puede entregar el mismo mensaje mas de una vez, aqui guardo los ya procesados
    private final Set<String> procesados = ConcurrentHashMap.newKeySet();

    public PubSubController(ObjectMapper objectMapper, NotificacionServicio notificacionServicio) {
        this.objectMapper = objectMapper;
        this.notificacionServicio = notificacionServicio;
    }

    @PostMapping("/transferencias")
    public ResponseEntity<Void> recibir(@RequestBody MensajePush push) {
        if (push.mensaje() == null || push.mensaje().datos() == null) {
            log.warn("Llego un mensaje vacio, se descarta");
            return ResponseEntity.noContent().build();
        }

        EventoTransferencia evento;
        try {
            byte[] json = Base64.getDecoder().decode(push.mensaje().datos());
            evento = objectMapper.readValue(json, EventoTransferencia.class);
        } catch (IllegalArgumentException | IOException e) {
            // si respondo error Pub/Sub lo reintenta para siempre, mejor lo descarto y lo dejo en el log
            log.error("No se pudo leer el mensaje {}", push.mensaje().idMensaje(), e);
            return ResponseEntity.noContent().build();
        }

        if (!procesados.add(evento.idTransferencia())) {
            log.info("La transferencia {} ya se habia procesado, se ignora", evento.idTransferencia());
            return ResponseEntity.noContent().build();
        }

        try {
            notificacionServicio.procesar(evento);
        } catch (RuntimeException e) {
            // la quito para que el reintento de Pub/Sub si la procese
            procesados.remove(evento.idTransferencia());
            throw e;
        }
        return ResponseEntity.noContent().build();
    }
}
