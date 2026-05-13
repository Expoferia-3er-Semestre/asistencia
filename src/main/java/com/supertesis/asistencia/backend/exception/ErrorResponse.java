// Standardized error response structure for API errors

package com.supertesis.asistencia.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // Omit null fields from JSON
public class ErrorResponse {

    // When the error occurred
    private final Instant timestamp;

    // HTTP status code
    private final int status;

    // Short error type description
    private final String error;

    // Human-readable error message
    private final String message;

    // Request path that caused the error
    private final String path;

    // Unique identifier for tracing this error in logs
    private final String traceId;

    // Validation errors for 400 Bad Request responses
    // Maps field name to list of validation messages
    private final Map<String, List<String>> validationErrors;

    // Additional details for debugging (only in dev/staging)
    private final Object details;
}