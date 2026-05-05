package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.RolPermisoDirecto;
import com.supertesis.asistencia.backend.entity.RolPermisoDirectoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolPermisoDirectoRepository extends JpaRepository<RolPermisoDirecto, RolPermisoDirectoId> {

    // Buscar permisos directos de un rol
    List<RolPermisoDirecto> findByRolId(Integer rolId);

    // Buscar roles que tienen un permiso directo
    List<RolPermisoDirecto> findByPermisoId(Integer permisoId);

    // Verificar si existe la relación rol-permiso
    boolean existsByRolIdAndPermisoId(Integer rolId, Integer permisoId);

    // Eliminar todas las relaciones de un rol
    void deleteByRolId(Integer rolId);

    // Eliminar todas las relaciones de un permiso
    void deleteByPermisoId(Integer permisoId);

    // Eliminar una relación rol-permiso específica
    void deleteByRolIdAndPermisoId(Integer rolId, Integer permisoId);

    // Obtener permisos directos con información completa
    @Query("SELECT rpd FROM RolPermisoDirecto rpd JOIN FETCH rpd.rol JOIN FETCH rpd.permiso WHERE rpd.rol.id = :rolId")
    List<RolPermisoDirecto> findByRolIdWithDetails(@Param("rolId") Integer rolId);
}