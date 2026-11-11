package com.danbanda.bancolite.cuentas.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CuentaControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Crear cuenta regresa 201 con un numero de 10 digitos")
    void crearCuenta() throws Exception {
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titular": "Dan Banda", "saldoInicial": 1000.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.numeroCuenta").value(matchesPattern("\\d{10}")))
                .andExpect(jsonPath("$.titular").value("Dan Banda"))
                .andExpect(jsonPath("$.moneda").value("MXN"));
    }

    @Test
    @DisplayName("Cargo y abono actualizan el saldo en la base")
    void cargoYAbono() throws Exception {
        String numero = crearCuenta("1000.00");

        mover(numero, "cargo", "200.00").andExpect(status().isOk());
        mover(numero, "abono", "50.00").andExpect(status().isOk());

        assertThat(saldoDe(numero)).isEqualByComparingTo("850.00");
    }

    @Test
    @DisplayName("Cargo sin fondos regresa 422 y no toca el saldo")
    void cargoSinFondos() throws Exception {
        String numero = crearCuenta("100.00");

        mover(numero, "cargo", "500.00")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Fondos insuficientes"))
                .andExpect(jsonPath("$.detail").value("Fondos insuficientes en la cuenta " + numero));

        assertThat(saldoDe(numero)).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Consultar una cuenta que no existe regresa 404")
    void cuentaNoExiste() throws Exception {
        mockMvc.perform(get("/api/cuentas/{numero}", "0000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la cuenta 0000000000"));
    }

    @Test
    @DisplayName("Crear cuenta sin titular regresa 400 con el campo que fallo")
    void crearSinTitular() throws Exception {
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"saldoInicial": 100.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.titular").value("El titular es obligatorio"));
    }

    @Test
    @DisplayName("Un monto negativo regresa 400")
    void montoNegativo() throws Exception {
        String numero = crearCuenta("100.00");

        mover(numero, "cargo", "-50")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.monto").value("El monto debe ser mayor a cero"));
    }

    // ---- metodos de apoyo ----

    private String crearCuenta(String saldoInicial) throws Exception {
        String json = mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titular\": \"Prueba\", \"saldoInicial\": " + saldoInicial + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("numeroCuenta").asText();
    }

    private org.springframework.test.web.servlet.ResultActions mover(String numero, String operacion, String monto)
            throws Exception {
        return mockMvc.perform(post("/api/cuentas/{numero}/{operacion}", numero, operacion)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"monto\": " + monto + "}"));
    }

    private BigDecimal saldoDe(String numero) throws Exception {
        String json = mockMvc.perform(get("/api/cuentas/{numero}", numero))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("saldo").decimalValue();
    }
}
