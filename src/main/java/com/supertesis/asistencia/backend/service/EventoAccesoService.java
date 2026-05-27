package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.eventoacceso.EventoAccesoResponseDto;
import com.supertesis.asistencia.backend.entity.Asistencia;
import com.supertesis.asistencia.backend.entity.AsistenciaEstado;
import com.supertesis.asistencia.backend.entity.DispositivoAcceso;
import com.supertesis.asistencia.backend.entity.DispositivoTipo;
import com.supertesis.asistencia.backend.entity.EventoAcceso;
import com.supertesis.asistencia.backend.entity.EventoAccesoMetodo;
import com.supertesis.asistencia.backend.entity.EventoAccesoResultado;
import com.supertesis.asistencia.backend.entity.EventoAccesoTipo;
import com.supertesis.asistencia.backend.entity.Personal;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.mapper.EventoAccesoMapper;
import com.supertesis.asistencia.backend.repository.AsistenciaRepository;
import com.supertesis.asistencia.backend.repository.DispositivoAccesoRepository;
import com.supertesis.asistencia.backend.repository.EventoAccesoRepository;
import com.supertesis.asistencia.backend.repository.PersonalRepository;
import com.supertesis.asistencia.backend.security.JwtUtils; // Paquete actualizado
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException; 

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventoAccesoService {

    private final JwtUtils jwtUtils;
    private final PersonalRepository personalRepository;
    private final EventoAccesoRepository eventoAccesoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final EventoAccesoMapper eventoAccesoMapper;
    private final DispositivoAccesoRepository dispositivoAccesoRepository;

    @Transactional
    public EventoAccesoResponseDto procesarScan(String qrToken) {
        
        // 1. Validar JWT
        if (!jwtUtils.validateJwtToken(qrToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }

        // 2. Extraer Claims y verificar tipo
        Claims claims = jwtUtils.getClaimsFromToken(qrToken);
        String tipoToken = claims.get("tipo", String.class);
        if (tipoToken == null || !tipoToken.equals("qr")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token no válido para acceso físico");
        }

        // 3. Extraer personalId del Subject
        Integer personalId;
        try {
            personalId = Integer.parseInt(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de personal corrupto en el token");
        }

        // 4. Buscar personal en BD 
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", personalId.toString()));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(23, 59, 59, 999999999);

        // 5. Buscar último EventoAcceso para determinar el estado actual del bucle
        Optional<EventoAcceso> ultimoEventoOpt = eventoAccesoRepository
                .findUltimoEventoDelDia(personalId, inicioDia, finDia);

        // MODIFICACIÓN DE FLUJO: Conexión cíclica e infinita de eventos para la Demo
        EventoAccesoTipo proximoTipoEvento = EventoAccesoTipo.entrada; // Por defecto si es el primero del día
        
        if (ultimoEventoOpt.isPresent()) {
            EventoAccesoTipo estadoUltimo = ultimoEventoOpt.get().getTipoEvento();
            if (estadoUltimo.equals(EventoAccesoTipo.entrada)) {
                proximoTipoEvento = EventoAccesoTipo.salida;
            } else if (estadoUltimo.equals(EventoAccesoTipo.salida)) {
                // SOLUCIÓN: En lugar de denegar el acceso, reiniciamos el ciclo a una nueva entrada
                proximoTipoEvento = EventoAccesoTipo.entrada;
            }
        }

        // 6. Insertar en eventos_acceso (Auditoría intacta e incremental)
        DispositivoAcceso dispositivo = dispositivoAccesoRepository
                .findFirstByTipoAndActivoTrue(DispositivoTipo.qr)
                .orElseThrow(() -> new ResourceNotFoundException("DispositivoAcceso", "tipo=qr"));
                
        EventoAcceso nuevoEvento = new EventoAcceso();
        nuevoEvento.setPersonal(personal);
        nuevoEvento.setDispositivo(dispositivo);
        nuevoEvento.setTimestampEvento(ahora);
        nuevoEvento.setTipoEvento(proximoTipoEvento);
        nuevoEvento.setMetodo(EventoAccesoMetodo.qr);
        nuevoEvento.setResultado(EventoAccesoResultado.exitoso);
        nuevoEvento = eventoAccesoRepository.save(nuevoEvento);

        // 7. Upsert Dinámico en Asistencia
        LocalTime horaActual = ahora.toLocalTime();

        if (proximoTipoEvento.equals(EventoAccesoTipo.entrada)) {
            // SOLUCIÓN EXPOFERIA: Buscamos si hay un bloque de asistencia activo (es decir, que no tenga salida registrada aún)
            Optional<Asistencia> asistenciaActivaOpt = asistenciaRepository
                    .findAsistenciaActivaParaHoy(personalId, hoy); // Método sugerido en Repository o usando filtros en streams

            Asistencia asistencia;
            if (asistenciaActivaOpt.isEmpty()) {
                // Si no hay ninguna marca hoy, o la última ya se cerró con salida, generamos una fila NUEVA en la UI
                asistencia = new Asistencia();
                asistencia.setPersonal(personal);
                asistencia.setFecha(hoy);
                asistencia.setEstado(AsistenciaEstado.presente);
                asistencia.setCorregido(false);
                asistencia.setCreadoEn(ahora);
            } else {
                // Caso hipotético de seguridad: reusar la existente si quedó abierta
                asistencia = asistenciaActivaOpt.get();
            }
            
            asistencia.setHoraEntrada(horaActual);
            asistencia.setEventoEntrada(nuevoEvento);
            asistencia.setActualizadoEn(ahora);
            asistenciaRepository.save(asistencia);

        } else {
            // Es un registro de SALIDA: Debe cerrar la marca de entrada que se encuentre abierta actualmente
            Optional<Asistencia> asistenciaAbiertaOpt = asistenciaRepository
                    .findFirstByPersonalIdAndFechaAndHoraSalidaIsNullOrderByHoraEntradaDesc(personalId, hoy);

            if (asistenciaAbiertaOpt.isPresent()) {
                Asistencia asistenciaExistente = asistenciaAbiertaOpt.get();
                asistenciaExistente.setHoraSalida(horaActual);
                asistenciaExistente.setEventoSalida(nuevoEvento);
                asistenciaExistente.setActualizadoEn(ahora);
                asistenciaRepository.save(asistenciaExistente);
            } else {
                // Fallback defensivo si escanean una salida huérfana
                Asistencia asistenciaInconsistente = new Asistencia();
                asistenciaInconsistente.setPersonal(personal);
                asistenciaInconsistente.setFecha(hoy);
                asistenciaInconsistente.setHoraSalida(horaActual);
                asistenciaInconsistente.setEventoSalida(nuevoEvento);
                asistenciaInconsistente.setEstado(AsistenciaEstado.ausente);
                asistenciaInconsistente.setCorregido(false);
                asistenciaInconsistente.setCreadoEn(ahora);
                asistenciaInconsistente.setActualizadoEn(ahora);
                asistenciaRepository.save(asistenciaInconsistente);
            }
        }

        String mensajeFeedback = "Registro de " + proximoTipoEvento + " para la Expoferia procesado correctamente.";
        return eventoAccesoMapper.toResponseDto(nuevoEvento, mensajeFeedback);
    }
    
    
    
    
    /*
     procesarScan con funcionamiento oficial
      @Transactional
    public EventoAccesoResponseDto procesarScan(String qrToken) {

        // 1. Validar JWT usando tu método existente
        if (!jwtUtils.validateJwtToken(qrToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }

        // 2. Extraer Claims usando el nuevo método público y verificar tipo
        Claims claims = jwtUtils.getClaimsFromToken(qrToken);
        String tipoToken = claims.get("tipo", String.class);
        if (tipoToken == null || !tipoToken.equals("qr")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token no válido para acceso físico");
        }

        // 3. Extraer personalId del Subject
        Integer personalId;
        try {
            personalId = Integer.parseInt(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de personal corrupto en el token");
        }

        // 4. Buscar personal en BD
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", personalId.toString()));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(23, 59, 59, 999999999);

        // 5. Buscar último EventoAcceso
        Optional<EventoAcceso> ultimoEventoOpt = eventoAccesoRepository
                .findUltimoEventoDelDia(personalId, inicioDia, finDia);

        EventoAccesoTipo proximoTipoEvento = EventoAccesoTipo.desconocido; // Valor por defecto en caso de denegación
        if (ultimoEventoOpt.isEmpty()) {
            proximoTipoEvento = EventoAccesoTipo.entrada;
        } else {
            EventoAccesoTipo estadoUltimo = ultimoEventoOpt.get().getTipoEvento();
            if (estadoUltimo.equals(EventoAccesoTipo.entrada)) {
                proximoTipoEvento = EventoAccesoTipo.salida;
            } else if (estadoUltimo.equals(EventoAccesoTipo.salida)) {
                return EventoAccesoResponseDto.conMensaje("Ya registró entrada y salida hoy");
            }
        }

        // 6. Insertar en eventos_acceso
        DispositivoAcceso dispositivo = dispositivoAccesoRepository
                .findFirstByTipoAndActivoTrue(DispositivoTipo.qr)
                .orElseThrow(() -> new ResourceNotFoundException("DispositivoAcceso", "tipo=qr"));
        EventoAcceso nuevoEvento = new EventoAcceso();
        nuevoEvento.setPersonal(personal);

        nuevoEvento.setDispositivo(dispositivo);
        nuevoEvento.setTimestampEvento(ahora);
        nuevoEvento.setTipoEvento(proximoTipoEvento);
        nuevoEvento.setMetodo(EventoAccesoMetodo.qr);
        nuevoEvento.setResultado(EventoAccesoResultado.exitoso);
        nuevoEvento = eventoAccesoRepository.save(nuevoEvento);

        // 7. Upsert en asistencia
        Optional<Asistencia> asistenciaOpt = asistenciaRepository.findByPersonalIdAndFecha(personalId, hoy);
        LocalTime horaActual = ahora.toLocalTime();

        if (proximoTipoEvento.equals(EventoAccesoTipo.entrada)) {
            Asistencia asistencia = asistenciaOpt.orElse(new Asistencia());
            asistencia.setPersonal(personal);
            asistencia.setFecha(hoy);
            asistencia.setHoraEntrada(horaActual);
            asistencia.setEventoEntrada(nuevoEvento);
            asistencia.setEstado(AsistenciaEstado.presente);
            // AGREGAR ESTOS:
            asistencia.setCorregido(false);
            asistencia.setCreadoEn(ahora);
            asistencia.setActualizadoEn(ahora);
            asistenciaRepository.save(asistencia);

        } else {
            if (asistenciaOpt.isPresent()) {
                Asistencia asistenciaExistente = asistenciaOpt.get();
                asistenciaExistente.setHoraSalida(horaActual);
                asistenciaExistente.setEventoSalida(nuevoEvento);
                // AGREGAR ESTE:
                asistenciaExistente.setActualizadoEn(ahora);
                asistenciaRepository.save(asistenciaExistente);
            } else {
                Asistencia asistenciaInconsistente = new Asistencia();
                asistenciaInconsistente.setPersonal(personal);
                asistenciaInconsistente.setFecha(hoy);
                asistenciaInconsistente.setHoraSalida(horaActual);
                asistenciaInconsistente.setEventoSalida(nuevoEvento);
                asistenciaInconsistente.setEstado(AsistenciaEstado.ausente);
                // AGREGAR ESTOS:
                asistenciaInconsistente.setCorregido(false);
                asistenciaInconsistente.setCreadoEn(ahora);
                asistenciaInconsistente.setActualizadoEn(ahora);
                asistenciaRepository.save(asistenciaInconsistente);
            }
        }

        String mensajeFeedback = "Registro de " + proximoTipoEvento + " procesado correctamente.";
        return eventoAccesoMapper.toResponseDto(nuevoEvento, mensajeFeedback);
    } */
}