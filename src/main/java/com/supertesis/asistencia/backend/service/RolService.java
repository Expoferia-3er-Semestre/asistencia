package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Rol;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    public Rol findById(Integer id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id.toString()));
    }

    public Rol findByNombreRol(String nombreRol) {
        return rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", nombreRol));
    }

    @Transactional
    public Rol save(Rol rol) {
        if (rol.getId() == null && rolRepository.existsByNombreRol(rol.getNombreRol())) {
            throw new IllegalArgumentException("Ya existe un rol con nombre " + rol.getNombreRol());
        }
        return rolRepository.save(rol);
    }

    @Transactional
    public Rol update(Integer id, Rol updatedRol) {
        Rol existing = findById(id);
        existing.setNombreRol(updatedRol.getNombreRol());
        existing.setDescripcion(updatedRol.getDescripcion());
        return rolRepository.save(existing);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!rolRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rol", id.toString());
        }
        rolRepository.deleteById(id);
    }

}
