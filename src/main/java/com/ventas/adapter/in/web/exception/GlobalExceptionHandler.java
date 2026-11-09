package com.ventas.adapter.in.web.exception;

import com.ventas.application.exception.ExistenciaInsuficienteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        Map<String, Object> problemDetail = new HashMap<String, Object>();
        problemDetail.put("type", "https://api.tusistema.com/errors/bad-request");
        problemDetail.put("title", "Bad Request");
        problemDetail.put("status", HttpStatus.BAD_REQUEST.value());
        problemDetail.put("detail", "Error de validación en los campos: " + errors);

        return new ResponseEntity<Object>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExistenciaInsuficienteException.class)
    public ResponseEntity<Object> handleExistenciaInsuficiente(ExistenciaInsuficienteException ex) {
        Map<String, Object> problemDetail = new HashMap<String, Object>();
        problemDetail.put("type", "https://api.tusistema.com/errors/conflict");
        problemDetail.put("title", "Conflict");
        problemDetail.put("status", HttpStatus.CONFLICT.value());
        problemDetail.put("detail", ex.getMessage());

        return new ResponseEntity<Object>(problemDetail, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> problemDetail = new HashMap<String, Object>();
        problemDetail.put("type", "https://api.tusistema.com/errors/not-found");
        problemDetail.put("title", "Not Found");
        problemDetail.put("status", HttpStatus.NOT_FOUND.value());
        problemDetail.put("detail", ex.getMessage() != null ? ex.getMessage() : "El recurso solicitado no fue encontrado.");

        return new ResponseEntity<Object>(problemDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        Map<String, Object> problemDetail = new HashMap<String, Object>();
        problemDetail.put("type", "https://api.tusistema.com/errors/internal-server-error");
        problemDetail.put("title", "Internal Server Error");
        problemDetail.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        problemDetail.put("detail", "Ocurrió un error interno en el servidor.");

        return new ResponseEntity<Object>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
