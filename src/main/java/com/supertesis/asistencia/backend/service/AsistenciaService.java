package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.asistencia.AsistenciaResponseDto;
import com.supertesis.asistencia.backend.entity.Asistencia;
import com.supertesis.asistencia.backend.entity.AsistenciaEstado;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.mapper.AsistenciaMapper;
import com.supertesis.asistencia.backend.repository.AsistenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final AsistenciaMapper asistenciaMapper;

    /**
     * Resuelve las consultas dinámicas con cualquier combinación de filtros.
     * Aplica las reglas de negocio por defecto descritas en los criterios de aceptación.
     */
    public List<AsistenciaResponseDto> findWithFilters(
            LocalDate fecha, LocalDate fechaDesde, LocalDate fechaHasta,
            Integer personalId, Integer departamentoId, AsistenciaEstado estado) {

        // Regla: Si no hay parámetros de fecha ni rango, pero hay personalId -> Último mes por defecto
        if (fecha == null && fechaDesde == null && fechaHasta == null && personalId != null) {
            fechaDesde = LocalDate.now().minusMonths(1);
            fechaHasta = LocalDate.now();
        }
        
        // Regla: Si no se envía absolutamente ningún filtro de tiempo -> Devuelve hoy
        if (fecha == null && fechaDesde == null && fechaHasta == null) {
            fecha = LocalDate.now();
        }

        // Delegamos al repositorio dinámico
        return asistenciaRepository.findDynamicWithFilters(
                fecha, fechaDesde, fechaHasta, personalId, departamentoId, estado)
                .stream()
                .map(asistenciaMapper::toResponseDto)
                .toList();
    }

    /**
     * Obtiene estadísticas agregadas de los estados de asistencia en un rango de fechas.
     */
    public Map<String, Long> getEstadisticas(LocalDate fechaDesde, LocalDate fechaHasta) {
        // Consultamos todas las asistencias en el rango de fechas (dejando los demás filtros en null)
        List<Asistencia> asistencias = asistenciaRepository.findDynamicWithFilters(
                null, fechaDesde, fechaHasta, null, null, null);

        // Agrupamos por el nombre del estado y contamos la cantidad de cada uno
        return asistencias.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getEstado().name(),
                        Collectors.counting()
                ));
    }

    public AsistenciaResponseDto findById(Integer id) {
        return asistenciaMapper.toResponseDto(asistenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia", id.toString())));
    }

    @Transactional
    public AsistenciaResponseDto save(Asistencia asistencia) {
        if (asistenciaRepository.findByPersonalIdAndFecha(asistencia.getPersonal().getId(), asistencia.getFecha()).isPresent()) {
            throw new IllegalArgumentException("Ya existe asistencia registrada para este personal en la fecha especificada");
        }
        return asistenciaMapper.toResponseDto(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponseDto update(Integer id, Asistencia updatedAsistencia) {
        Asistencia existing = asistenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia", id.toString()));
        
        existing.setEstado(updatedAsistencia.getEstado());
        existing.setObservaciones(updatedAsistencia.getObservaciones());
        existing.setActualizadoEn(LocalDateTime.now());
        
        return asistenciaMapper.toResponseDto(asistenciaRepository.save(existing));
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!asistenciaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asistencia", id.toString());
        }
        asistenciaRepository.deleteById(id);
    }

    // Obtener historial de un personal en un rango de fechas
    public List<AsistenciaResponseDto> findHistorial(Integer personalId, LocalDate fechaDesde, LocalDate fechaHasta) {
        return asistenciaRepository.findByPersonalIdAndFechasWithPersonal(personalId, fechaDesde, fechaHasta)
                .stream()
                .map(asistenciaMapper::toResponseDto)
                .toList();
    }

    public List<AsistenciaResponseDto> findAll() {
        return asistenciaRepository.findAll()
                .stream()
                .map(asistenciaMapper::toResponseDto)
                .toList();
    }

    // Obtener registros de un día específico
    public List<AsistenciaResponseDto> findByFecha(LocalDate fecha) {
        return asistenciaRepository.findByFechaWithPersonal(fecha)
                .stream()
                .map(asistenciaMapper::toResponseDto)
                .toList();
    }

    public List<AsistenciaResponseDto> findByPersonalId(Integer personalId) {
        return asistenciaRepository.findByPersonalIdAndFechaBetween(personalId, LocalDate.now().minusMonths(1), LocalDate.now())
                .stream()
                .map(asistenciaMapper::toResponseDto)
                .toList();
    }

}