package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.Asistencia;
import com.supertesis.asistencia.backend.entity.AsistenciaEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {


    
    /**
     * Busca una asistencia específica por empleado y fecha.
     * Útil para la validación antes de hacer un save() y evitar duplicados.
     */
    Optional<Asistencia> findByPersonalIdAndFecha(Integer personalId, LocalDate fecha);

    /**
     * Consulta dinámica principal con JPQL.
     * Evalúa cada parámetro: si es NULL, ignora ese filtro; si tiene valor, lo aplica.
     * Hace un JOIN FETCH con 'personal' para optimizar la carga perezosa (LAZY).
     */
    @Query("SELECT a FROM Asistencia a " +
           "JOIN FETCH a.personal p " +
           "WHERE (:fecha IS NULL OR a.fecha = :fecha) " +
           "AND (:fechaDesde IS NULL OR a.fecha >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR a.fecha <= :fechaHasta) " +
           "AND (:personalId IS NULL OR p.id = :personalId) " +
           "AND (:departamentoId IS NULL OR p.departamento.id = :departamentoId) " +
           "AND (:estado IS NULL OR a.estado = :estado) " +
           "ORDER BY a.fecha DESC, p.id ASC")
    List<Asistencia> findDynamicWithFilters(
            @Param("fecha") LocalDate fecha,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("personalId") Integer personalId,
            @Param("departamentoId") Integer departamentoId,
            @Param("estado") AsistenciaEstado estado);

    // JOIN FETCH para traer la asistencia y el personal en una sola consulta
    @Query("SELECT a FROM Asistencia a JOIN FETCH a.personal p WHERE a.fecha = :fecha")
    List<Asistencia> findByFechaWithPersonal(@Param("fecha") LocalDate fecha);

    // JOIN FETCH para el historial con rango de fechas
    @Query("SELECT a FROM Asistencia a JOIN FETCH a.personal p WHERE p.id = :personalId AND a.fecha BETWEEN :fechaDesde AND :fechaHasta ORDER BY a.fecha DESC")
    List<Asistencia> findByPersonalIdAndFechasWithPersonal(
            @Param("personalId") Integer personalId, 
            @Param("fechaDesde") LocalDate fechaDesde, 
            @Param("fechaHasta") LocalDate fechaHasta);

    // Asistencia de un personal en un rango de fechas
    List<Asistencia> findByPersonalIdAndFechaBetween(Integer personalId, LocalDate fechaInicio, LocalDate fechaFin);

    // Asistencia por fecha
    List<Asistencia> findByFecha(LocalDate fecha);

    // Asistencia por estado
    List<Asistencia> findByEstado(AsistenciaEstado estado);

    // Asistencia por turno
    List<Asistencia> findByTurnoId(Integer turnoId);

    // Asistencia corregida
    List<Asistencia> findByCorregidoTrue();

    // Asistencia corregida por usuario
    List<Asistencia> findByCorregidoPorId(Integer corregidoPorId);

    // Asistencia de personal en fecha específica
    @Query("SELECT a FROM Asistencia a WHERE a.personal.id = :personalId AND a.fecha = :fecha")
    Optional<Asistencia> findAsistenciaByPersonalAndFecha(@Param("personalId") Integer personalId, @Param("fecha") LocalDate fecha);

    // Asistencia con información del personal y turno
    @Query("SELECT a FROM Asistencia a JOIN FETCH a.personal p LEFT JOIN FETCH a.turno t WHERE a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Asistencia> findAsistenciaWithDetails(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Conteo de asistencia por estado en un rango de fechas
    @Query("SELECT a.estado, COUNT(a) FROM Asistencia a WHERE a.fecha BETWEEN :fechaInicio AND :fechaFin GROUP BY a.estado")
    List<Object[]> countAsistenciaByEstado(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Asistencia tardía o con salida anticipada
    @Query("SELECT a FROM Asistencia a WHERE a.estado IN ('tardanza', 'salida_anticipada') AND a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Asistencia> findAsistenciaIrregular(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Verificar si existe asistencia para personal y fecha
    boolean existsByPersonalIdAndFecha(Integer personalId, LocalDate fecha);
}