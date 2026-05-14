// Centralized exception handling for all controllers

package com.supertesis.asistencia.backend.exception;
// GlobalExceptionHandler.java
// Production-ready global exception handler

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(BusinessException.class)
                public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(ex.getStatus().value())
                        .error(ex.getErrorCode())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .traceId(UUID.randomUUID().toString()) // Generamos un ID único para rastreo
                        .build();

                return new ResponseEntity<>(error, ex.getStatus());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
                java.util.Map<String, List<String>> validationErrors = new java.util.HashMap<>();
    
                // Extraemos los errores de cada campo
                ex.getBindingResult().getFieldErrors().forEach(f -> {
                        validationErrors
                        .computeIfAbsent(f.getField(), key -> new ArrayList<>())
                        .add(f.getDefaultMessage());
                });

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message("Datos de entrada inválidos")
                        .path(request.getRequestURI())
                        .validationErrors(validationErrors) // Aquí pasamos el mapa
                        .traceId(UUID.randomUUID().toString())
                        .build());
        }

        @ExceptionHandler(Exception.class)
                public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("INTERNAL_SERVER_ERROR")
                        .message("Ocurrió un error inesperado en el servidor")
                        .path(request.getRequestURI())
                        .traceId(UUID.randomUUID().toString())
                        // Aquí podrías usar tu propiedad 'app.include-error-details' para añadir ex.getMessage() si estás en dev
                        .build());
        }



}