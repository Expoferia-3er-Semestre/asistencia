package com.supertesis.asistencia.backend.dto.eventoacceso;

import java.time.LocalTime;

public record EventoAccesoResponseDto(
    String tipoEvento,
    String nombrePersonal,
    String cedula,
    LocalTime horaRegistro,
    String mensaje
) {
    // Constructor estático de conveniencia para mensajes informativos (ej. Límite alcanzado)
    public static EventoAccesoResponseDto conMensaje(String mensaje) {
        return new EventoAccesoResponseDto(null, null, null, null, mensaje);
    }
}