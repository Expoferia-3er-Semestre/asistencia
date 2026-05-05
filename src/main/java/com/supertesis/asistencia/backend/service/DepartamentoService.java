package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Departamento;
import com.supertesis.asistencia.backend.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public List<Departamento> findAll() {
        return departamentoRepository.findAll();
    }

    public Departamento findById(Integer id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado con id " + id));
    }

    public List<Departamento> findActive() {
        return departamentoRepository.findByActivoTrue();
    }

    public List<Departamento> findByNombreContaining(String nombre) {
        return departamentoRepository.findByNombreContaining(nombre);
    }

    @Transactional
    public Departamento save(Departamento departamento) {
        if (departamento.getId() == null && departamentoRepository.existsByNombre(departamento.getNombre())) {
            throw new IllegalArgumentException("Ya existe un departamento con nombre " + departamento.getNombre());
        }
        return departamentoRepository.save(departamento);
    }

    @Transactional
    public Departamento update(Integer id, Departamento updatedDepartamento) {
        Departamento existing = findById(id);
        existing.setNombre(updatedDepartamento.getNombre());
        existing.setDescripcion(updatedDepartamento.getDescripcion());
        existing.setActivo(updatedDepartamento.getActivo());
        return departamentoRepository.save(existing);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!departamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Departamento no encontrado con id " + id);
        }
        departamentoRepository.deleteById(id);
    }
}
