package com.supertesis.asistencia.backend.dto.cargo;

import com.supertesis.asistencia.backend.dto.RelacionSencillaDto;

public record CargoResponseDto(
    Integer id,
    String nombre,
    RelacionSencillaDto departamento
) {}