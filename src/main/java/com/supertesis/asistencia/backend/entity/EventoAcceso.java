package com.supertesis.asistencia.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
import jakarta.persistence.Lob;

@Entity
@Table(name = "eventos_acceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id")
    private Personal personal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private DispositivoAcceso dispositivo;

    @Column(name = "timestamp_evento", nullable = false)
    private LocalDateTime timestampEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 20)
    private EventoAccesoTipo tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventoAccesoMetodo metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventoAccesoResultado resultado;

    @Lob
    @Column(name = "datos_raw")
    private String datosRaw;
















}