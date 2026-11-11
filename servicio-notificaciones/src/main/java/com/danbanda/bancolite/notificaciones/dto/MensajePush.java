package com.danbanda.bancolite.notificaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// formato en el que Pub/Sub manda los mensajes por push, los nombres del JSON los define Google
public record MensajePush(
        @JsonProperty("message") Mensaje mensaje,
        @JsonProperty("subscription") String suscripcion
) {
    public record Mensaje(
            @JsonProperty("data") String datos,
            @JsonProperty("messageId") String idMensaje,
            @JsonProperty("publishTime") String fechaPublicacion
    ) {
    }
}
