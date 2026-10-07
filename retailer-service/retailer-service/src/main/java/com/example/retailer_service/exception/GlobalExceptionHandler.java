package com.example.retailer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.authorization.AuthorizationDeniedException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================
    // RESPONSE STATUS EXCEPTION
    // =====================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>>
    handleResponseStatusException(ResponseStatusException ex) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                ex.getStatusCode().value()
        );

        body.put(
                "error",
                ex.getStatusCode().toString()
        );

        body.put(
                "message",
                ex.getReason()
        );

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(body);
    }

    // =====================================================
    // VALIDATION ERROR
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        body.put(
                "error",
                "Bad Request"
        );

        body.put(
                "message",
                "Validation failed"
        );

        body.put(
                "errors",
                errors
        );

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    // =====================================================
    // ACCESS DENIED
    // =====================================================

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>>
    handleAuthorizationDenied(AuthorizationDeniedException ex) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.FORBIDDEN.value()
        );

        body.put(
                "error",
                "Forbidden"
        );

        body.put(
                "message",
                "Access Denied"
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    // =====================================================
    // GENERAL RUNTIME EXCEPTION
    // =====================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>>
    handleRuntimeException(RuntimeException ex) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        body.put(
                "error",
                "Bad Request"
        );

        body.put(
                "message",
                ex.getMessage()
        );

        return ResponseEntity
                .badRequest()
                .body(body);
    }
}