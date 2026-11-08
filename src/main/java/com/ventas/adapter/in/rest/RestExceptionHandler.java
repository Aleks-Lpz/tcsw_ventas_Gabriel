package com.ventas.adapter.in.rest;

import com.ventas.adapter.in.rest.dto.ProblemaDetalle;
import com.ventas.model.ExistenciaInsuficienteException;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte errores de validación y de dominio en respuestas ProblemaDetalle (400 / 409) según docs/openapi.yaml.
 */
@RestControllerAdvice(basePackageClasses = RestExceptionHandler.class)
public class RestExceptionHandler {

    private static final String TIPO_BASE = "https://ejemplo.invalid/problemas/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemaDetalle> validacion(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return problema(HttpStatus.BAD_REQUEST, "entrada-invalida", "Entrada inválida", detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemaDetalle> cuerpoIlegible(HttpMessageNotReadableException e) {
        return problema(HttpStatus.BAD_REQUEST, "entrada-invalida", "Entrada inválida",
                "El cuerpo de la petición no es JSON válido o no cumple el esquema.");
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ProblemaDetalle> productoNoEncontrado(ProductoNoEncontradoException e) {
        return problema(HttpStatus.BAD_REQUEST, "producto-inexistente", "Producto inexistente", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemaDetalle> reglaDeNegocio(IllegalArgumentException e) {
        return problema(HttpStatus.BAD_REQUEST, "entrada-invalida", "Entrada inválida", e.getMessage());
    }

    @ExceptionHandler(ExistenciaInsuficienteException.class)
    public ResponseEntity<ProblemaDetalle> existenciaInsuficiente(ExistenciaInsuficienteException e) {
        return problema(HttpStatus.CONFLICT, "existencia-insuficiente", "Existencia insuficiente", e.getMessage());
    }

    private static ResponseEntity<ProblemaDetalle> problema(HttpStatus status, String tipo, String titulo,
                                                            String detalle) {
        return ResponseEntity.status(status)
                .body(new ProblemaDetalle(TIPO_BASE + tipo, titulo, status.value(), detalle));
    }
}
