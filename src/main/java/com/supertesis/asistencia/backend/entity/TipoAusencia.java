package com.supertesis.asistencia.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipos_ausencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoAusencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "requiere_documento", nullable = false)
    private Boolean requiereDocumento;

    @Column(name = "descuenta_sueldo", nullable = false)
    private Boolean descuentaSueldo;

    @Column(nullable = false)
    private Boolean activo;










}