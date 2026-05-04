package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Asistencia;
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

    public List<Asistencia> findAll() {
        return asistenciaRepository.findAll();
    }

    public Asistencia findById(Integer id) {
        return asistenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada con id " + id));
    }

    public List<Asistencia> findByFecha(LocalDate fecha) {
        return asistenciaRepository.findByFecha(fecha);
    }

    public List<Asistencia> findByPersonalId(Integer personalId) {
        return asistenciaRepository.findByPersonalIdAndFechaBetween(personalId, LocalDate.now().minusMonths(1), LocalDate.now());
    }

    @Transactional
    public Asistencia save(Asistencia asistencia) {
        // Validar que no exista asistencia para el mismo personal y fecha
        if (asistenciaRepository.findByPersonalIdAndFecha(asistencia.getPersonal().getId(), asistencia.getFecha()).isPresent()) {
            throw new IllegalArgumentException("Ya existe asistencia registrada para este personal en la fecha especificada");
        }
        return asistenciaRepository.save(asistencia);
    }

    @Transactional
    public Asistencia update(Integer id, Asistencia updatedAsistencia) {
        Asistencia existing = findById(id);
        // Actualizar campos necesarios
        existing.setEstado(updatedAsistencia.getEstado());
        existing.setObservaciones(updatedAsistencia.getObservaciones());
        existing.setActualizadoEn(java.time.LocalDateTime.now());
        return asistenciaRepository.save(existing);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!asistenciaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asistencia no encontrada con id " + id);
        }
        asistenciaRepository.deleteById(id);
    }
}