package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Búsqueda por nombre del rol (único)
    Optional<Rol> findByNombreRol(String nombreRol);

    // Búsqueda por nombre que contenga
    List<Rol> findByNombreRolContaining(String nombre);

    // Verificar si existe nombre del rol
    boolean existsByNombreRol(String nombreRol);
}