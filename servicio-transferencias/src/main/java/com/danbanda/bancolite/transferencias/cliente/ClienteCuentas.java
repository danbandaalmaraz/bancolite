package com.danbanda.bancolite.transferencias.cliente;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Component
public class ClienteCuentas {

    private final RestClient restClient;

    public ClienteCuentas(RestClient.Builder builder,
                          @Value("${bancolite.cuentas.url}") String urlCuentas) {
        // nunca llamar a otro servicio sin timeout, si no el hilo se queda esperando para siempre
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(3));
        fabrica.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = builder
                .baseUrl(urlCuentas)
                .requestFactory(fabrica)
                .build();
    }

    public void validarQueExiste(String numeroCuenta) {
        ejecutar("consulta", numeroCuenta, () -> restClient.get()
                .uri("/api/cuentas/{numero}", numeroCuenta)
                .retrieve()
                .toBodilessEntity());
    }

    public void cargar(String numeroCuenta, BigDecimal monto) {
        mover(numeroCuenta, "cargo", monto);
    }

    public void abonar(String numeroCuenta, BigDecimal monto) {
        mover(numeroCuenta, "abono", monto);
    }

    private void mover(String numeroCuenta, String operacion, BigDecimal monto) {
        ejecutar(operacion, numeroCuenta, () -> restClient.post()
                .uri("/api/cuentas/{numero}/{operacion}", numeroCuenta, operacion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("monto", monto))
                .retrieve()
                .toBodilessEntity());
    }

    private void ejecutar(String operacion, String numeroCuenta, Runnable llamada) {
        try {
            llamada.run();
        } catch (HttpClientErrorException e) {
            throw new OperacionCuentaExcepcion(obtenerDetalle(e), e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new CuentasNoDisponiblesExcepcion(
                    "No se pudo hacer la " + operacion + " de la cuenta " + numeroCuenta, e);
        }
    }

    // saca el "detail" del ProblemDetail que regresa nuestro ManejadorExcepciones de cuentas
    private String obtenerDetalle(HttpClientErrorException e) {
        try {
            ProblemDetail problema = e.getResponseBodyAs(ProblemDetail.class);
            if (problema != null && problema.getDetail() != null) {
                return problema.getDetail();
            }
        } catch (RuntimeException ignorada) {
            // si el cuerpo no se puede leer uso el mensaje generico
        }
        return "El servicio de cuentas rechazo la operacion (" + e.getStatusCode().value() + ")";
    }
}
