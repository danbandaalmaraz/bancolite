package com.danbanda.bancolite.transferencias.excepcion;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(TransferenciaInvalidaExcepcion.class)
    public ProblemDetail transferenciaInvalida(TransferenciaInvalidaExcepcion ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problema.setTitle("Transferencia invalida");
        return problema;
    }

    @ExceptionHandler(TransferenciaNoEncontradaExcepcion.class)
    public ProblemDetail noEncontrada(TransferenciaNoEncontradaExcepcion ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Transferencia no encontrada");
        return problema;
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail faltaHeader(MissingRequestHeaderException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Falta el header " + ex.getHeaderName());
        problema.setTitle("Header obligatorio");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La solicitud tiene datos invalidos");
        problema.setTitle("Datos invalidos");
        problema.setProperty("errores", errores);
        return problema;
    }

    // dos peticiones con la misma clave al mismo tiempo: la segunda choca con el unique de la base
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail claveDuplicada(DataIntegrityViolationException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Ya hay una transferencia en proceso con esa clave de idempotencia");
        problema.setTitle("Conflicto");
        return problema;
    }
}
