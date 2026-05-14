package com.supertesis.asistencia.backend.dto;

/**
 * Record auxiliar para no enviar la entidad completa
 */

public record RelacionSencillaDto(
    Integer id, 
    String nombre
) {}
