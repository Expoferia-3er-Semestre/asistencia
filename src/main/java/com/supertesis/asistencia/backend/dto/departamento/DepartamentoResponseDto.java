package com.supertesis.asistencia.backend.dto.departamento;

/**
 * DTO para devolver la información de un Departamento.
 * Se usa Record por su inmutabilidad y sintaxis concisa.
 */
public record DepartamentoResponseDto(
    Integer id,
    String nombre,
    String descripcion,
    Boolean activo
) {}