package com.danbanda.bancolite.notificaciones.servicio;

import com.danbanda.bancolite.notificaciones.dto.EventoTransferencia;
import com.danbanda.bancolite.notificaciones.modelo.Alerta;
import com.danbanda.bancolite.notificaciones.modelo.TipoAlerta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DetectorFraude {

    private static final BigDecimal MONTO_ALTO = new BigDecimal("50000.00");
    private static final int MAXIMO_TRANSFERENCIAS = 3;
    private static final Duration VENTANA = Duration.ofMinutes(10);

    // por cada cuenta guardo las fechas de sus transferencias recientes
    private final Map<String, Deque<Instant>> historialPorCuenta = new ConcurrentHashMap<>();
    private final List<Alerta> alertas = new CopyOnWriteArrayList<>();

    public List<Alerta> evaluar(EventoTransferencia evento) {
        List<Alerta> nuevas = new ArrayList<>();

        if (evento.monto().compareTo(MONTO_ALTO) >= 0) {
            nuevas.add(new Alerta(evento.idTransferencia(), evento.cuentaOrigen(), TipoAlerta.MONTO_ALTO,
                    "Transferencia de " + evento.monto() + " MXN", Instant.now()));
        }

        int recientes = registrarYContar(evento.cuentaOrigen(), evento.fecha());
        if (recientes > MAXIMO_TRANSFERENCIAS) {
            nuevas.add(new Alerta(evento.idTransferencia(), evento.cuentaOrigen(), TipoAlerta.ALTA_FRECUENCIA,
                    recientes + " transferencias en menos de " + VENTANA.toMinutes() + " minutos", Instant.now()));
        }

        alertas.addAll(nuevas);
        return nuevas;
    }

    public List<Alerta> obtenerAlertas() {
        return List.copyOf(alertas);
    }

    // ventana deslizante: agrego la nueva fecha y saco las que ya tienen mas de 10 minutos
    private int registrarYContar(String cuenta, Instant fecha) {
        Deque<Instant> historial = historialPorCuenta.computeIfAbsent(cuenta, c -> new ArrayDeque<>());
        synchronized (historial) {
            historial.addLast(fecha);
            Instant limite = fecha.minus(VENTANA);
            while (!historial.isEmpty() && historial.peekFirst().isBefore(limite)) {
                historial.pollFirst();
            }
            return historial.size();
        }
    }
}
