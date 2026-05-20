package com.supertesis.asistencia.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supertesis.asistencia.backend.entity.EventoAcceso;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventoAccesoRepository extends JpaRepository<EventoAcceso, Long> {
    
    // Busca el último evento registrado en el rango de hoy
    @Query("SELECT e FROM EventoAcceso e WHERE e.personal.id = :personalId " +
           "AND e.timestampEvento BETWEEN :inicioDia AND :finDia " +
           "ORDER BY e.timestampEvento DESC LIMIT 1")
    Optional<EventoAcceso> findUltimoEventoDelDia(
        @Param("personalId") Integer personalId, 
        @Param("inicioDia") LocalDateTime inicioDia, 
        @Param("finDia") LocalDateTime finDia
    );
}