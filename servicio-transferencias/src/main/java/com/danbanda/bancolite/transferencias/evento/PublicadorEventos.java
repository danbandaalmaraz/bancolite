package com.danbanda.bancolite.transferencias.evento;

import com.danbanda.bancolite.transferencias.modelo.Transferencia;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorEventos.class);

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public PublicadorEventos(PubSubTemplate pubSubTemplate, ObjectMapper objectMapper,
                             @Value("${bancolite.pubsub.topic}") String topic) {
        this.pubSubTemplate = pubSubTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    // si falla la publicacion solo se registra en el log, la transferencia ya quedo hecha
    public void publicar(Transferencia transferencia) {
        try {
            String json = objectMapper.writeValueAsString(EventoTransferencia.desde(transferencia));

            pubSubTemplate.publish(topic, json)
                    .whenComplete((idMensaje, error) -> {
                        if (error != null) {
                            log.error("No se pudo publicar el evento de la transferencia {}", transferencia.getId(), error);
                        } else {
                            log.info("Evento de la transferencia {} publicado con id {}", transferencia.getId(), idMensaje);
                        }
                    });
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Error al preparar el evento de la transferencia {}", transferencia.getId(), e);
        }
    }
}
