// ResourceNotFoundException.java
// Thrown when a requested resource does not exist

package com.supertesis.asistencia.backend.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends BusinessException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(
            String.format("%s not found with id: %s", resourceType, resourceId),
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND"
        );
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
}