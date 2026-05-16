package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalRepository extends JpaRepository<Personal, Integer> {

    // Búsqueda por cédula (única)
    Optional<Personal> findByCedula(String cedula);

    // Búsqueda por correo (único)
    Optional<Personal> findByCorreo(String correo);

    // Búsqueda por nombre y apellido
    List<Personal> findByNombreAndApellido(String nombre, String apellido);

    // Búsqueda por departamento
    List<Personal> findByDepartamentoId(Integer departamentoId);

    // Búsqueda por cargo
    List<Personal> findByCargoId(Integer cargoId);

    // Personal activo
    List<Personal> findByActivo(Boolean activo);

    // Consulta personalizada con JOIN
    @Query("SELECT p FROM Personal p JOIN p.departamento d WHERE d.nombre = :departamentoNombre")
    List<Personal> findByDepartamentoNombre(@Param("departamentoNombre") String departamentoNombre);

    // Verificar si existe cédula
    boolean existsByCedula(String cedula);

    // Verificar si existe correo
    boolean existsByCorreo(String correo);
}