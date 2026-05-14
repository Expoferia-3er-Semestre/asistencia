package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Integer> {

    List<Cargo> findByDepartamentoId(Integer departamentoId);

    List<Cargo> findByNombreContaining(String nombreCargo);
}
