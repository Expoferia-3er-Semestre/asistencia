package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.departamento.DepartamentoRequestDto;
import com.supertesis.asistencia.backend.dto.departamento.DepartamentoResponseDto;
import com.supertesis.asistencia.backend.entity.Departamento;
import com.supertesis.asistencia.backend.exception.ConflictException;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.mapper.DepartamentoMapper;
import com.supertesis.asistencia.backend.repository.DepartamentoRepository;
import com.supertesis.asistencia.backend.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final PersonalRepository personalRepository;
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
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id.toString()));

        if (personalRepository.existsByDepartamentoIdAndActivoTrue(id)) {
            throw new ConflictException("Departamento", "activo",
                    "No se puede desactivar el departamento porque tiene personal activo asociado.");
        }

        departamento.setActivo(false);
        departamentoRepository.save(departamento);
    }

    @Transactional
    public void desactivar(Integer id) {
        deleteById(id);
    }

    @Transactional
    public void activar(Integer id) {
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id.toString()));
        if (Boolean.TRUE.equals(departamento.getActivo())) {
            return;
        }
        departamento.setActivo(true);
        departamentoRepository.save(departamento);
    }
}
