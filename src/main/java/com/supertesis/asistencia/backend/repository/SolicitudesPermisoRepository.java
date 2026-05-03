package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.SolicitudEstado;
import com.supertesis.asistencia.backend.entity.SolicitudesPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SolicitudesPermisoRepository extends JpaRepository<SolicitudesPermiso, Integer> {

    // Solicitudes por personal
    List<SolicitudesPermiso> findByPersonalId(Integer personalId);

    // Solicitudes por estado
    List<SolicitudesPermiso> findByEstado(SolicitudEstado estado);

    // Solicitudes pendientes
    List<SolicitudesPermiso> findByEstadoOrderByCreadoEnDesc(SolicitudEstado estado);

    // Solicitudes por tipo de ausencia
    List<SolicitudesPermiso> findByTipoAusenciaId(Integer tipoAusenciaId);

    // Solicitudes revisadas por usuario
    List<SolicitudesPermiso> findByRevisadoPorId(Integer revisadoPorId);

    // Solicitudes en un rango de fechas
    List<SolicitudesPermiso> findByFechaInicioBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Solicitudes que incluyen una fecha específica
    @Query("SELECT sp FROM SolicitudesPermiso sp WHERE sp.fechaInicio <= :fecha AND sp.fechaFin >= :fecha")
    List<SolicitudesPermiso> findSolicitudesThatIncludeDate(@Param("fecha") LocalDate fecha);

    // Solicitudes pendientes de un departamento
    @Query("SELECT sp FROM SolicitudesPermiso sp JOIN sp.personal p WHERE p.departamento.id = :departamentoId AND sp.estado = 'pendiente'")
    List<SolicitudesPermiso> findSolicitudesPendientesByDepartamento(@Param("departamentoId") Integer departamentoId);

    // Conteo de solicitudes por estado
    @Query("SELECT sp.estado, COUNT(sp) FROM SolicitudesPermiso sp GROUP BY sp.estado")
    List<Object[]> countSolicitudesByEstado();
}