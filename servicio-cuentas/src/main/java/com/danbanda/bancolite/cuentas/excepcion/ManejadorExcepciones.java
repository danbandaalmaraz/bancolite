package com.danbanda.bancolite.cuentas.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(CuentaNoEncontradaExcepcion.class)
    public ProblemDetail cuentaNoEncontrada(CuentaNoEncontradaExcepcion ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Cuenta no encontrada");
        return problema;
    }

    @ExceptionHandler(FondosInsuficientesExcepcion.class)
    public ProblemDetail fondosInsuficientes(FondosInsuficientesExcepcion ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problema.setTitle("Fondos insuficientes");
        return problema;
    }

    // cuando falla algun @NotNull, @DecimalMin, etc. de los DTO
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

    // la salta el @Version de Cuenta cuando dos operaciones mueven la misma cuenta a la vez
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail conflictoConcurrencia(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La cuenta fue modificada por otra operacion, intenta de nuevo");
        problema.setTitle("Conflicto");
        return problema;
    }
}
