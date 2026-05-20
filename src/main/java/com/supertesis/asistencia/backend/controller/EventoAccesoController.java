package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.dto.eventoacceso.EventoAccesoRequestDto;
import com.supertesis.asistencia.backend.dto.eventoacceso.EventoAccesoResponseDto;
import com.supertesis.asistencia.backend.service.EventoAccesoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eventos-acceso")
@RequiredArgsConstructor
public class EventoAccesoController {

    private final EventoAccesoService eventoAccesoService;

    @PostMapping
    public ResponseEntity<EventoAccesoResponseDto> registrarAcceso(@Valid @RequestBody EventoAccesoRequestDto request) {
        // Al ser un record, accedemos al valor del token usando request.qrToken()
        EventoAccesoResponseDto response = eventoAccesoService.procesarScan(request.qrToken());
        return ResponseEntity.ok(response);
    }
}