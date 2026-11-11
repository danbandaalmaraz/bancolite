package com.danbanda.bancolite.notificaciones.servicio;

import com.danbanda.bancolite.notificaciones.dto.EventoTransferencia;
import com.danbanda.bancolite.notificaciones.modelo.Alerta;
import com.danbanda.bancolite.notificaciones.modelo.TipoAlerta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DetectorFraudeTest {

    private static final Instant AHORA = Instant.parse("2026-11-10T12:00:00Z");

    private DetectorFraude detector;

    @BeforeEach
    void configurar() {
        detector = new DetectorFraude();
    }

    private EventoTransferencia evento(String monto, Instant fecha) {
        return new EventoTransferencia(UUID.randomUUID().toString(), "1111111111", "2222222222",
                new BigDecimal(monto), "COMPLETADA", null, fecha);
    }

    @Test
    @DisplayName("Una transferencia de 50,000 o mas genera alerta de monto alto")
    void montoAlto() {
        List<Alerta> alertas = detector.evaluar(evento("50000.00", AHORA));

        assertThat(alertas).extracting(Alerta::tipo).containsExactly(TipoAlerta.MONTO_ALTO);
    }

    @Test
    @DisplayName("Un centavo abajo del limite no genera alerta")
    void montoNormal() {
        assertThat(detector.evaluar(evento("49999.99", AHORA))).isEmpty();
    }

    @Test
    @DisplayName("La cuarta transferencia en 10 minutos genera alerta de alta frecuencia")
    void altaFrecuencia() {
        detector.evaluar(evento("100", AHORA));
        detector.evaluar(evento("100", AHORA.plusSeconds(60)));
        detector.evaluar(evento("100", AHORA.plusSeconds(120)));

        List<Alerta> alertas = detector.evaluar(evento("100", AHORA.plusSeconds(180)));

        assertThat(alertas).extracting(Alerta::tipo).containsExactly(TipoAlerta.ALTA_FRECUENCIA);
    }

    @Test
    @DisplayName("Las transferencias de hace mas de 10 minutos ya no cuentan")
    void fueraDeLaVentana() {
        detector.evaluar(evento("100", AHORA));
        detector.evaluar(evento("100", AHORA.plusSeconds(60)));
        detector.evaluar(evento("100", AHORA.plusSeconds(120)));

        // 11 minutos despues de la primera: esa ya salio de la ventana
        List<Alerta> alertas = detector.evaluar(evento("100", AHORA.plusSeconds(660)));

        assertThat(alertas).isEmpty();
    }
}
