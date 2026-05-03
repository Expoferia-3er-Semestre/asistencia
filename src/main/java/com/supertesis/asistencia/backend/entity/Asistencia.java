package com.supertesis.asistencia.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "asistencia", uniqueConstraints = @UniqueConstraint(columnNames = {"personal_id", "fecha"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_entrada_id")
    private EventoAcceso eventoEntrada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_salida_id")
    private EventoAcceso eventoSalida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AsistenciaEstado estado;

    @Column(name = "minutos_tardanza")
    private Short minutosTardanza;

    @Column(name = "minutos_extra")
    private Short minutosExtra;

    @Column(nullable = false)
    private Boolean corregido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corregido_por")
    private Usuario corregidoPor;

    @Column(name = "motivo_correccion", columnDefinition = "TEXT")
    private String motivoCorreccion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

}