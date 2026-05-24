package com.supertesis.asistencia.backend.dto;

public record AuthResponseDto(
    String username,
    String nombre,
    String rol,       // "ROLE_ADMIN", "ROLE_ASISTENTE", "ROLE_ESCANER"
    String mensaje
) {}