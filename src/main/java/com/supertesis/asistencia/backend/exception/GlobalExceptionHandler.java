package com.supertesis.asistencia.backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        System.out.println("\n[HANDLER] BusinessException atrapada!");
        System.out.println("Causa: " + ex.getMessage());
        System.out.println("Path: " + request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getStatus().value())
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        System.out.println("\n[HANDLER] MethodArgumentNotValidException atrapada (Error de validación)");
        
        java.util.Map<String, List<String>> validationErrors = new java.util.HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(f -> {
            System.out.println("Campo: " + f.getField() + " -> Error: " + f.getDefaultMessage());
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
                .validationErrors(validationErrors)
                .traceId(UUID.randomUUID().toString())
                .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        System.err.println("\n[DATABASE ERROR] DataIntegrityViolationException atrapada!");
        
        // Obtener el mensaje más específico de la excepción
        String causaTecnica = ex.getMostSpecificCause() != null ? 
                              ex.getMostSpecificCause().getMessage() : ex.getMessage();
                              
        System.err.println("Detalle técnico: " + causaTecnica);
        System.out.println("Path: " + request.getRequestURI());

        // Mensaje por defecto en caso de que no coincida con ninguna FK conocida
        String mensajeUsuario = "No se puede realizar la operación porque el registro está relacionado con otros datos del sistema.";

        // Analizar la causa para personalizar el mensaje según la Foreign Key
        if (causaTecnica != null) {
            if (causaTecnica.contains("fk_cargo_departamento")) {
                mensajeUsuario = "No se puede eliminar el departamento: tiene cargos asociados.";
            } else if (causaTecnica.contains("fk_personal_cargo")) {
                mensajeUsuario = "No se puede eliminar el cargo: tiene personal asociado.";
            } else if (causaTecnica.contains("fk_at_turno")) {
                mensajeUsuario = "No se puede eliminar el turno: está asignado a personal.";
            } else if (causaTecnica.contains("fk_sp_tipo")) {
                mensajeUsuario = "No se puede eliminar el tipo de ausencia: tiene solicitudes.";
            } else if (causaTecnica.contains("fk_ea_dispositivo")) {
                mensajeUsuario = "No se puede eliminar el dispositivo: tiene eventos registrados.";
            } else if (causaTecnica.contains("fk_usuario_rol")) {
                mensajeUsuario = "No se puede eliminar el rol: tiene usuarios asignados.";
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .error("DATABASE_INTEGRITY_CONFLICT")
                .message(mensajeUsuario)
                .path(request.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        System.err.println("\n[CRITICAL ERROR] Exception inesperada atrapada!");
        ex.printStackTrace(); // Esto imprimirá toda la pila de error en consola para que puedas debuguear

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("Ocurrió un error inesperado en el servidor")
                .path(request.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .build());
    }
}