package com.supertesis.asistencia.backend.dto.personal;

import java.time.LocalDate;

import com.supertesis.asistencia.backend.dto.RelacionSencillaDto;

/**
 * Record para respuestas detalladas de Personal.
 * Incluye objetos anidados para Cargo y Departamento.
 */
public record PersonalResponseDto(
    Integer id,
    String nombre,
    String apellido,
    String cedula,
    String correo,
    String telefono,
    RelacionSencillaDto cargo,
    RelacionSencillaDto departamento,
    LocalDate fechaEgreso,
    LocalDate fechaIngreso,
    Boolean activo
) {}
