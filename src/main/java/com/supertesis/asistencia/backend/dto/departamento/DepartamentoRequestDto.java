package com.supertesis.asistencia.backend.dto.departamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para recibir la información de un Departamento en las solicitudes de creación o actualización.
 * Se usa Record por su inmutabilidad y sintaxis concisa.
 */

public record DepartamentoRequestDto(
    @NotBlank(message = "El nombre del departamento es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    String nombre,
    String descripcion,
    Boolean activo
) {}