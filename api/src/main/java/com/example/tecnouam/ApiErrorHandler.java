package com.example.tecnouam;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, String>> api(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst().orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(Map.of("error", detail));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> malformed(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "JSON o fecha inválida"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> integrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Conflicto de integridad de datos"));
    }
}
