package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Permiso;
import com.supertesis.asistencia.backend.entity.Rol;
import com.supertesis.asistencia.backend.entity.RolPermisoDirecto;
import com.supertesis.asistencia.backend.repository.PermisoRepository;
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
public class RolPermisoDirectoService {

    private final RolPermisoDirectoRepository rolPermisoDirectoRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public List<RolPermisoDirecto> findAll() {
        return rolPermisoDirectoRepository.findAll();
    }

    public List<RolPermisoDirecto> findByRolId(Integer rolId) {
        return rolPermisoDirectoRepository.findByRolId(rolId);
    }

    public List<RolPermisoDirecto> findByPermisoId(Integer permisoId) {
        return rolPermisoDirectoRepository.findByPermisoId(permisoId);
    }

    public boolean existsByRolIdAndPermisoId(Integer rolId, Integer permisoId) {
        return rolPermisoDirectoRepository.existsByRolIdAndPermisoId(rolId, permisoId);
    }

    @Transactional
    public RolPermisoDirecto assignPermisoToRol(Integer rolId, Integer permisoId) {
        if (rolPermisoDirectoRepository.existsByRolIdAndPermisoId(rolId, permisoId)) {
            throw new IllegalArgumentException("El permiso ya está asignado a este rol");
        }
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id " + rolId));
        Permiso permiso = permisoRepository.findById(permisoId)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id " + permisoId));
        RolPermisoDirecto relacion = new RolPermisoDirecto(rol, permiso);
        return rolPermisoDirectoRepository.save(relacion);
    }

    @Transactional
    public void removePermisoFromRol(Integer rolId, Integer permisoId) {
        if (!rolPermisoDirectoRepository.existsByRolIdAndPermisoId(rolId, permisoId)) {
            throw new ResourceNotFoundException("La relación rol-permiso no existe");
        }
        rolPermisoDirectoRepository.deleteByRolIdAndPermisoId(rolId, permisoId);
    }

    public List<Permiso> findPermisosByRol(Integer rolId) {
        return rolPermisoDirectoRepository.findByRolIdWithDetails(rolId).stream()
                .map(RolPermisoDirecto::getPermiso)
                .collect(Collectors.toList());
    }

    public List<Rol> findRolesByPermiso(Integer permisoId) {
        return rolPermisoDirectoRepository.findByPermisoId(permisoId).stream()
                .map(RolPermisoDirecto::getRol)
                .collect(Collectors.toList());
    }
}
