package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Integer> {

    List<Cargo> findByDepartamentoId(Integer departamentoId);

    List<Cargo> findByNombreContaining(String nombreCargo);

    // Para el save: ¿Existe este nombre en este departamento?
    boolean existsByNombreAndDepartamentoId(String nombre, Integer departamentoId);
    
    // Para el update: ¿Existe este nombre en este departamento, pero en UN CARGO DISTINTO al que estoy editando?
    boolean existsByNombreAndDepartamentoIdAndIdNot(String nombre, Integer departamentoId, Integer id);

    boolean existsByNombre(String nombre);

}
