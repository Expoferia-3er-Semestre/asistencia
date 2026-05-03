package com.supertesis.asistencia.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "personal", uniqueConstraints = @UniqueConstraint(columnNames = "cedula"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100, name = "nombre")
    private String nombre;

    @Column(nullable = false, length = 100, name = "apellido")
    private String apellido;

    @Column(nullable = false, length = 20, unique = true, name = "cedula")
    private String cedula;

    @Column(nullable = true, length = 150, name = "correo")
    private String correo;

    @Column(nullable = true, length = 20, name = "telefono")
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_egreso")
    private LocalDate fechaEgreso;

    @Column(nullable = false, name = "activo")
    private Boolean activo;






















}
