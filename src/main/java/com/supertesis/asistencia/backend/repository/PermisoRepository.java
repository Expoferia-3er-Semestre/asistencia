package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    Optional<Permiso> findByNombre(String nombre);

    List<Permiso> findByNombreContaining(String nombre);

    boolean existsByNombre(String nombre);
}
