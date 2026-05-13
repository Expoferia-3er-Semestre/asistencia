package com.supertesis.asistencia.backend.dto.cargo;

/**
 * Representación simplificada del departamento dentro del cargo
 */
record DepartamentoSencilloDto(Integer id, String nombre) {}

public record CargoResponseDto(
    Integer id,
    String nombreCargo,
    DepartamentoSencilloDto departamento
) {}