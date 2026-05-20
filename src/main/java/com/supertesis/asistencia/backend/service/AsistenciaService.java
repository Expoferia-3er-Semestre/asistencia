package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.asistencia.AsistenciaResponseDto;
import com.supertesis.asistencia.backend.entity.Asistencia;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.mapper.AsistenciaMapper;
import com.supertesis.asistencia.backend.repository.AsistenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final AsistenciaMapper asistenciaMapper;

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

    public AsistenciaResponseDto findById(Integer id) {
        return asistenciaMapper.toResponseDto(asistenciaRepository.findById(id).orElseThrow(() -> 
        new ResourceNotFoundException("Asistencia", id.toString())));
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

    @Transactional
    public AsistenciaResponseDto save(Asistencia asistencia) {
        // Validar que no exista asistencia para el mismo personal y fecha
        if (asistenciaRepository.findByPersonalIdAndFecha(asistencia.getPersonal().getId(), asistencia.getFecha()).isPresent()) {
            throw new IllegalArgumentException("Ya existe asistencia registrada para este personal en la fecha especificada");
        }
        return asistenciaMapper.toResponseDto(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponseDto update(Integer id, Asistencia updatedAsistencia) {
        Asistencia existing = asistenciaRepository.findById(id).orElseThrow(() -> 
        new ResourceNotFoundException("Asistencia", id.toString()));
        // Actualizar campos necesarios
        existing.setEstado(updatedAsistencia.getEstado());
        existing.setObservaciones(updatedAsistencia.getObservaciones());
        existing.setActualizadoEn(java.time.LocalDateTime.now());
        return asistenciaMapper.toResponseDto(asistenciaRepository.save(existing));
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!asistenciaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asistencia", id.toString());
        }
        asistenciaRepository.deleteById(id);
    }
}