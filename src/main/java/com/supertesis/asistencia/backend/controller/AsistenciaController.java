package com.supertesis.asistencia.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supertesis.asistencia.backend.dto.asistencia.AsistenciaResponseDto;
import com.supertesis.asistencia.backend.service.AsistenciaService;

import lombok.RequiredArgsConstructor;

@RestController // Indica que esta clase es un controlador API REST
@RequestMapping("/api/asistencia") // Define la ruta base para este controlador
@RequiredArgsConstructor // Genera un constructor con los campos finales (inyección de dependencias)
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @GetMapping
    public ResponseEntity<List<AsistenciaResponseDto>> consultarAsistencia(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) Integer personalId,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta) {

        // Caso 1: GET /api/asistencia?personalId=&fechaDesde=&fechaHasta=
        if (personalId != null && fechaDesde != null && fechaHasta != null) {
            return ResponseEntity.ok(asistenciaService.findHistorial(personalId, fechaDesde, fechaHasta));
        }

        // Caso 2: GET /api/asistencia?fecha=2026-05-21
        if (fecha != null) {
            return ResponseEntity.ok(asistenciaService.findByFecha(fecha));
        }

        // Si no mandan parámetros correctos, devolvemos un 400 Bad Request
        return ResponseEntity.badRequest().build(); 
    }
}