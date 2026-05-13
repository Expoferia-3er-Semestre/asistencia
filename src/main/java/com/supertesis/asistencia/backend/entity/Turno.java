package com.supertesis.asistencia.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "tolerancia_min", nullable = false)
    private Short toleranciaMin;

    @Column(nullable = false)
    private Boolean lunes;

    @Column(nullable = false)
    private Boolean martes;

    @Column(nullable = false)
    private Boolean miercoles;

    @Column(nullable = false)
    private Boolean jueves;

    @Column(nullable = false)
    private Boolean viernes;

    @Column(nullable = false)
    private Boolean sabado;

    @Column(nullable = false)
    private Boolean domingo;

    @Column(nullable = false)
    private Boolean activo;


























}