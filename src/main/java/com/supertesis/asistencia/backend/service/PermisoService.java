package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Permiso;
import com.supertesis.asistencia.backend.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermisoService {

    private final PermisoRepository permisoRepository;

    public List<Permiso> findAll() {
        return permisoRepository.findAll();
    }

    public Permiso findById(Integer id) {
        return permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id " + id));
    }

    public List<Permiso> findByNombreContaining(String nombre) {
        return permisoRepository.findByNombreContaining(nombre);
    }

    @Transactional
    public Permiso save(Permiso permiso) {
        if (permiso.getId() == null && permisoRepository.existsByNombre(permiso.getNombre())) {
            throw new IllegalArgumentException("Ya existe un permiso con nombre " + permiso.getNombre());
        }
        return permisoRepository.save(permiso);
    }

    @Transactional
    public Permiso update(Integer id, Permiso updatedPermiso) {
        Permiso existing = findById(id);
        existing.setNombre(updatedPermiso.getNombre());
        existing.setDescripcion(updatedPermiso.getDescripcion());
        return permisoRepository.save(existing);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!permisoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permiso no encontrado con id " + id);
        }
        permisoRepository.deleteById(id);
    }
}
