package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Permiso;
import com.supertesis.asistencia.backend.entity.Rol;
import com.supertesis.asistencia.backend.entity.RolPermisoDirecto;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.repository.RolPermisoDirectoRepository;
import com.supertesis.asistencia.backend.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolService {

    private final RolRepository rolRepository;
    private final RolPermisoDirectoRepository rolPermisoDirectoRepository;

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

    public List<Permiso> findPermisosDirectos(Integer rolId) {
        List<RolPermisoDirecto> relaciones = rolPermisoDirectoRepository.findByRolIdWithDetails(rolId);
        return relaciones.stream().map(RolPermisoDirecto::getPermiso).collect(Collectors.toList());
    }
}
