package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Cargo;
import com.supertesis.asistencia.backend.repository.CargoRepository;
import com.supertesis.asistencia.backend.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CargoService {

    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;

    public List<Cargo> findAll() {
        return cargoRepository.findAll();
    }

    public Cargo findById(Integer id) {
        return cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado con id " + id));
    }

    public List<Cargo> findByDepartamentoId(Integer departamentoId) {
        return cargoRepository.findByDepartamentoId(departamentoId);
    }

    @Transactional
    public Cargo save(Cargo cargo) {
        if (cargo.getDepartamento() != null && cargo.getDepartamento().getId() != null) {
            departamentoRepository.findById(cargo.getDepartamento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado con id " + cargo.getDepartamento().getId()));
        }
        return cargoRepository.save(cargo);
    }

    @Transactional
    public Cargo update(Integer id, Cargo updatedCargo) {
        Cargo existing = findById(id);
        existing.setNombreCargo(updatedCargo.getNombreCargo());
        existing.setDepartamento(updatedCargo.getDepartamento());
        return cargoRepository.save(existing);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!cargoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cargo no encontrado con id " + id);
        }
        cargoRepository.deleteById(id);
    }
}
