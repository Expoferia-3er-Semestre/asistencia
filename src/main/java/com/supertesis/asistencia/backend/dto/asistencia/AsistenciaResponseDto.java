package com.supertesis.asistencia.backend.dto.asistencia;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record AsistenciaResponseDto(
    Integer id,
    Integer personalId,
    String nombrePersonal,
    LocalDate fecha,
    
    @JsonFormat(pattern = "HH:mm")
    LocalTime horaEntrada,
    
    @JsonFormat(pattern = "HH:mm")
    LocalTime horaSalida,
    
    String estado,
    Boolean corregido
) {}