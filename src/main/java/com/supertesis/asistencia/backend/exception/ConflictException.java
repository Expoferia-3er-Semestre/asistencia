// ConflictException.java
// Thrown when an operation conflicts with current state

package com.supertesis.asistencia.backend.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public ConflictException(String resourceType, String field, String value) {
        super(
            String.format("%s with %s '%s' already exists", resourceType, field, value),
            HttpStatus.CONFLICT,
            "DUPLICATE_RESOURCE"
        );
    }
}
