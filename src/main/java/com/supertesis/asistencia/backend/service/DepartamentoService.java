package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.departamento.DepartamentoRequestDto;
import com.supertesis.asistencia.backend.dto.departamento.DepartamentoResponseDto;
import com.supertesis.asistencia.backend.entity.Departamento;
import com.supertesis.asistencia.backend.exception.ConflictException;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.mapper.DepartamentoMapper;
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
    private final DepartamentoMapper deptoMapper;

    public List<DepartamentoResponseDto> findAll() {
        return departamentoRepository.findAll().stream()
                        .map(deptoMapper::toResponseDto)
                        .toList();
    }

    public DepartamentoResponseDto findById(Integer id) {
        return departamentoRepository.findById(id)
                .map(deptoMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id.toString()));
    }

    public List<DepartamentoResponseDto> findActive() {
        return departamentoRepository.findByActivoTrue().stream()
                .map(deptoMapper::toResponseDto)
                .toList();
    }

    public List<DepartamentoResponseDto> findByNombreContaining(String nombre) {
        return departamentoRepository.findByNombreContaining(nombre).stream()
                .map(deptoMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public DepartamentoResponseDto save(DepartamentoRequestDto request) {
        if (departamentoRepository.existsByNombre(request.nombre())) {
            throw new ConflictException("Departamento", "nombre", request.nombre());
        }

        Departamento departamentoSave = deptoMapper.toEntity((request));

        departamentoSave.setActivo(true); // Por defecto, activo es true
    
        return deptoMapper.toResponseDto(departamentoRepository.save(departamentoSave));
    }

    @Transactional
    public DepartamentoResponseDto update(Integer id, DepartamentoRequestDto updatedDepartamento) {
        Departamento existing = departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id.toString()));
        
        if (departamentoRepository.existsByNombre(updatedDepartamento.nombre()) && !existing.getNombre().equals(updatedDepartamento.nombre())) {
             throw new ConflictException("Departamento", "nombre", updatedDepartamento.nombre());
        }
        existing.setNombre(updatedDepartamento.nombre());
        existing.setDescripcion(updatedDepartamento.descripcion());

        if (updatedDepartamento.activo() != null) {
            existing.setActivo(updatedDepartamento.activo());
        }
        
        return deptoMapper.toResponseDto(departamentoRepository.save(existing));
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!departamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Departamento", id.toString());
        }
        departamentoRepository.deleteById(id);
    }
}
