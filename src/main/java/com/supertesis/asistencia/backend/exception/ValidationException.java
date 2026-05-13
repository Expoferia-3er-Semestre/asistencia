// ValidationException.java
// Thrown when request data fails validation

package com.supertesis.asistencia.backend.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ValidationException extends BusinessException {

    private final Map<String, List<String>> errors;

    public ValidationException(Map<String, List<String>> errors) {
        super("Validation failed", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        this.errors = errors;
    }
}