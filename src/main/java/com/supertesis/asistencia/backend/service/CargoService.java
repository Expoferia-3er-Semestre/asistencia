package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.cargo.CargoRequestDto;
import com.supertesis.asistencia.backend.dto.cargo.CargoResponseDto;
import com.supertesis.asistencia.backend.entity.Cargo;
import com.supertesis.asistencia.backend.entity.Departamento;
import com.supertesis.asistencia.backend.mapper.CargoMapper;
import com.supertesis.asistencia.backend.repository.CargoRepository;
import com.supertesis.asistencia.backend.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supertesis.asistencia.backend.exception.ConflictException;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CargoService {

    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    
    private final CargoMapper cargoMapper;

    public List<CargoResponseDto> findAll() {
        return cargoRepository.findAll().stream()
        .map(cargoMapper::toResponseDto)
        .toList();
    }

    public CargoResponseDto findById(Integer id) {
        return cargoRepository.findById(id)
        .map(cargoMapper::toResponseDto)
        .orElseThrow(() -> new ResourceNotFoundException("Cargo", id.toString()));
    }

    public List<CargoResponseDto> findByDepartamentoId(Integer departamentoId) {
        return cargoRepository.findByDepartamentoId(departamentoId).stream()
        .map(cargoMapper::toResponseDto)
        .toList();
    }

    @Transactional
    public CargoResponseDto save(CargoRequestDto request) {
        
        Departamento departamento = departamentoRepository.findById(request.departamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", request.departamentoId().toString()));
        
        if (cargoRepository.existsByNombre(request.nombre())) {
            throw new ConflictException("Cargo", "nombre", request.nombre());
        }
        
        Cargo cargo = cargoMapper.toEntity(request);
        cargo.setDepartamento(departamento); // Aseguramos la relación
        
        return cargoMapper.toResponseDto(cargoRepository.save(cargo));
    }

    @Transactional
    public CargoResponseDto update(Integer id, CargoRequestDto updated) {
        Cargo existing = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", id.toString()));
        
        existing.setNombre(updated.nombre());
        Departamento departamento = departamentoRepository.findById(updated.departamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", updated.departamentoId().toString()));
        existing.setDepartamento(departamento);
        return cargoMapper.toResponseDto(cargoRepository.save(existing));
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!cargoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cargo", id.toString());
        }
        cargoRepository.deleteById(id);
    }
}
