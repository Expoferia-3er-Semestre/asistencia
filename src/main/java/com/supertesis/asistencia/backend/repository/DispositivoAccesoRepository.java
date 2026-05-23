package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.DispositivoAcceso;
import com.supertesis.asistencia.backend.entity.DispositivoTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispositivoAccesoRepository extends JpaRepository<DispositivoAcceso, Integer> {

    Optional<DispositivoAcceso> findFirstByTipoAndActivoTrue(DispositivoTipo tipo);

    List<DispositivoAcceso> findByActivoTrue();

    List<DispositivoAcceso> findByTipo(DispositivoTipo tipo);

    Optional<DispositivoAcceso> findByNumeroSerie(String numeroSerie);

    boolean existsByNumeroSerie(String numeroSerie);
}