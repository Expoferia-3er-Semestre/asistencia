package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Personal;
import com.supertesis.asistencia.backend.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalService {

    private final PersonalRepository personalRepository;

    public List<Personal> findAll() {
        return personalRepository.findAll();
    }

    public Personal findById(Integer id) {
        return personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con id " + id));
    }

    public Personal findByCedula(String cedula) {
        return personalRepository.findByCedula(cedula)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con cédula " + cedula));
    }

    public List<Personal> findByDepartamentoId(Integer departamentoId) {
        return personalRepository.findByDepartamentoId(departamentoId);
    }

    public List<Personal> findByCargoId(Integer cargoId) {
        return personalRepository.findByCargoId(cargoId);
    }

    public List<Personal> findActive() {
        return personalRepository.findByActivoTrue();
    }

    @Transactional
    public Personal save(Personal personal) {
        if (personal.getId() == null && personalRepository.existsByCedula(personal.getCedula())) {
            throw new IllegalArgumentException("Ya existe un personal con cédula " + personal.getCedula());
        }
        if (personal.getActivo() == null) {
            personal.setActivo(true);
        }
        return personalRepository.save(personal);
    }

    @Transactional
    public Personal update(Integer id, Personal updatedPersonal) {
        Personal existing = findById(id);
        existing.setNombre(updatedPersonal.getNombre());
        existing.setApellido(updatedPersonal.getApellido());
        existing.setCedula(updatedPersonal.getCedula());
        existing.setCorreo(updatedPersonal.getCorreo());
        existing.setTelefono(updatedPersonal.getTelefono());
        existing.setCargo(updatedPersonal.getCargo());
        existing.setDepartamento(updatedPersonal.getDepartamento());
        existing.setFechaIngreso(updatedPersonal.getFechaIngreso());
        existing.setFechaEgreso(updatedPersonal.getFechaEgreso());
        if (updatedPersonal.getActivo() != null) {
            existing.setActivo(updatedPersonal.getActivo());
        }
        return personalRepository.save(existing);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!personalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Personal no encontrado con id " + id);
        }
        personalRepository.deleteById(id);
    }
}
