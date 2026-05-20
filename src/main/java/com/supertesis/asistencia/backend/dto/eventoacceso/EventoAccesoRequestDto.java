package com.supertesis.asistencia.backend.dto.eventoacceso;

import jakarta.validation.constraints.NotBlank;

public record EventoAccesoRequestDto(
    @NotBlank(message = "El token QR es obligatorio")
    String qrToken
) {}