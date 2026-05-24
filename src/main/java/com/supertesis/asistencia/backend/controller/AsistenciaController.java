package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.dto.asistencia.AsistenciaResponseDto;
import com.supertesis.asistencia.backend.entity.AsistenciaEstado;
import com.supertesis.asistencia.backend.service.AsistenciaService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asistencia")
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    /**
     * Consulta el listado de asistencias aplicando filtros dinámicos.
     * Si no se envían parámetros, la lógica por defecto se gestiona en el servicio.
     */
    @GetMapping
    public ResponseEntity<List<AsistenciaResponseDto>> findAll(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer personalId,
            @RequestParam(required = false) Integer departamentoId,
            @RequestParam(required = false) AsistenciaEstado estado) {
        
        return ResponseEntity.ok(
            asistenciaService.findWithFilters(
                fecha, fechaDesde, fechaHasta, personalId, departamentoId, estado
            )
        );
    }

    /**
     * Obtiene los detalles de una asistencia individual por su ID.
     * Útil para los flujos de edición en el frontend.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AsistenciaResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(asistenciaService.findById(id));
    }

    /**
     * Obtiene el conteo estadístico agrupado por estado en un rango de fechas.
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> getEstadisticas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        
        return ResponseEntity.ok(asistenciaService.getEstadisticas(fechaDesde, fechaHasta));
    }
}