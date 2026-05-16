package com.supertesis.asistencia.backend.dto.personal;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PersonalUpdateRequestDto(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "El apellido es obligatorio")
    String apellido,

    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 7, max = 20, message = "La cédula debe tener un formato válido")
    String cedula,

    @Email(message = "El formato del correo es inválido")
    String correo,

    String telefono,

    @NotNull(message = "El ID del cargo es obligatorio")
    Integer cargoId,

    @NotNull(message = "El ID del departamento es obligatorio")
    Integer departamentoId,

    @NotNull(message = "La fecha de ingreso es obligatoria")
    LocalDate fechaIngreso,

    LocalDate fechaEgreso,

    @NotNull(message = "El estado activo es obligatorio")
    Boolean activo
) {}