package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.personal.PersonalResponseDto;
import com.supertesis.asistencia.backend.dto.personal.PersonalUpdateRequestDto;
import com.supertesis.asistencia.backend.dto.personal.PersonalCreateRequestDto;
import com.supertesis.asistencia.backend.entity.Cargo;
import com.supertesis.asistencia.backend.entity.Departamento;
import com.supertesis.asistencia.backend.entity.Personal;
import com.supertesis.asistencia.backend.mapper.PersonalMapper;
import com.supertesis.asistencia.backend.repository.CargoRepository;
import com.supertesis.asistencia.backend.repository.DepartamentoRepository;
import com.supertesis.asistencia.backend.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supertesis.asistencia.backend.exception.ConflictException;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalService {

    private final PersonalRepository personalRepository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final PersonalMapper personalMapper;

    public List<PersonalResponseDto> findAll() {
        return personalRepository.findAll().stream()
        .map(personalMapper::toResponseDto)
        .toList();
    }

    public PersonalResponseDto findById(Integer id) {
        return personalRepository.findById(id).map(personalMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Personal",id.toString()));
    }

    public PersonalResponseDto findByCedula(String cedula) {
        return personalRepository.findByCedula(cedula)
                .map(personalMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", cedula));
    }

    public PersonalResponseDto findByCorreo(String correo) {
        return personalRepository.findByCorreo(correo)
                .map(personalMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", correo));
    }

    public List<PersonalResponseDto> findByDepartamentoId(Integer departamentoId) {
       return personalRepository.findByDepartamentoId(departamentoId)
                .stream()
                .map(personalMapper::toResponseDto).toList();
    }

    public List<PersonalResponseDto> findByCargoId(Integer cargoId) {
        return personalRepository.findByCargoId(cargoId).stream()
                .map(personalMapper::toResponseDto)
                .toList();
    }

    public List<PersonalResponseDto> findByActivo(Boolean activo) {
        return personalRepository.findByActivo(activo).stream()
                .map(personalMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public PersonalResponseDto save(PersonalCreateRequestDto request) {
        if (personalRepository.existsByCedula(request.cedula())) {
            throw new ConflictException("Personal", "cedula", request.cedula());
        }

        Departamento departamento = departamentoRepository.findById(request.departamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", request.departamentoId().toString()));

        Cargo cargo = cargoRepository.findById(request.cargoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", request.cargoId().toString()));

        Personal personal = personalMapper.toEntityCreate(request);
        personal.setActivo(true);
        personal.setDepartamento(departamento);
        personal.setCargo(cargo);

        return personalMapper.toResponseDto(personalRepository.save(personal));
    }

    @Transactional
    public PersonalResponseDto update(Integer id, PersonalUpdateRequestDto updatedPersonal) {
        Personal existing = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", id.toString()));

        // Validación de unicidad
        if (personalRepository.existsByCedula(updatedPersonal.cedula()) && !existing.getCedula().equals(updatedPersonal.cedula())) {
            throw new ConflictException("Personal", "cedula", updatedPersonal.cedula());
        }
        if (updatedPersonal.correo() != null && !updatedPersonal.correo().isBlank()) {
            if (personalRepository.existsByCorreo(updatedPersonal.correo()) && !updatedPersonal.correo().equals(existing.getCorreo())) {
                throw new ConflictException("Personal", "correo", updatedPersonal.correo());
            }
        }

        // 1. Guardamos el estado actual ANTES de mapear los nuevos datos
        boolean estabaActivo = existing.getActivo();

        // 2. Actualizamos la entidad existente (requiere @MappingTarget en tu mapper)
        personalMapper.updateEntityFromDto(updatedPersonal, existing);

        if (updatedPersonal.cargoId() != null) {
        // Si el ID del cargo cambió, lo buscamos y lo actualizamos
        if (existing.getCargo() == null || !existing.getCargo().getId().equals(updatedPersonal.cargoId())) {
            Cargo nuevoCargo = cargoRepository.findById(updatedPersonal.cargoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", updatedPersonal.cargoId().toString()));
            existing.setCargo(nuevoCargo);
        }
        }

        if (updatedPersonal.departamentoId() != null) {
            // Si el ID del departamento cambió, lo buscamos y lo actualizamos
            if (existing.getDepartamento() == null || !existing.getDepartamento().getId().equals(updatedPersonal.departamentoId())) {
                Departamento nuevoDepartamento = departamentoRepository.findById(updatedPersonal.departamentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Departamento", updatedPersonal.departamentoId().toString()));
                existing.setDepartamento(nuevoDepartamento);
            }
        }

        // 3. Lógica de negocio para activo/inactivo
        if (updatedPersonal.activo() != null) {
            boolean esActivo = updatedPersonal.activo();

            if (estabaActivo && !esActivo) {
                // Caso A: Se está dando de baja por primera vez
                // Si el DTO ya trae una fecha (ej. HR programó la baja), la usamos. Si no, usamos la fecha actual.
                if (updatedPersonal.fechaEgreso() != null) {
                    existing.setFechaEgreso(updatedPersonal.fechaEgreso());
                } else {
                    existing.setFechaEgreso(java.time.LocalDate.now());
                }
            } 
            else if (!estabaActivo && esActivo) {
                // Caso B: Reincorporación
                // Vuelve a estar activo, por lo tanto no tiene fecha de egreso
                existing.setFechaEgreso(null);
            }
            // Caso C: Ya estaba inactivo y sigue inactivo -> El mapper o la DB mantiene la fecha original
        }

        // No necesitas llamar a existing.setId(id) porque nunca reemplazamos el objeto
        return personalMapper.toResponseDto(personalRepository.save(existing));
    }

    @Transactional
    public void deleteById(Integer id) {
        
        // 1. Aplicar borrado lógico en lugar de un DELETE real
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", id.toString()));
        personal.setActivo(false);
        
        // 2. Guardar los cambios
        personalRepository.save(personal);
    }

    @Transactional
    public void reactivar(Integer id) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", id.toString()));
        
        if (personal.getActivo()) {
            throw new IllegalStateException("El empleado ya se encuentra activo.");
        }
        
        personal.setActivo(true);
        personalRepository.save(personal);
    }
}
