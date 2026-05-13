package com.supertesis.asistencia.backend.dto.cargo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación y actualización de cargos.
 * Contiene el nombre del cargo y el ID del departamento al que pertenece.
 */

public record CargoRequestDto(
    @NotBlank(message = "El nombre del cargo es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    String nombre,
    
    @NotNull(message = "El ID del departamento es obligatorio")
    Integer departamentoId
) {}