package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.eventoacceso.EventoAccesoResponseDto;
import com.supertesis.asistencia.backend.entity.Asistencia;
import com.supertesis.asistencia.backend.entity.AsistenciaEstado;
import com.supertesis.asistencia.backend.entity.DispositivoAcceso;
import com.supertesis.asistencia.backend.entity.EventoAcceso;
import com.supertesis.asistencia.backend.entity.EventoAccesoMetodo;
import com.supertesis.asistencia.backend.entity.EventoAccesoResultado;
import com.supertesis.asistencia.backend.entity.EventoAccesoTipo;
import com.supertesis.asistencia.backend.entity.Personal;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.mapper.EventoAccesoMapper;
import com.supertesis.asistencia.backend.repository.AsistenciaRepository;
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

        EventoAccesoTipo proximoTipoEvento = null;

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
        DispositivoAcceso dispositivo = new DispositivoAcceso();
        dispositivo.setId(2); // ID fijo para QR, o podrías parametrizarlo si tienes varios dispositivos
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
            asistenciaRepository.save(asistencia);
            
        } else { 
            if (asistenciaOpt.isPresent()) {
                Asistencia asistenciaExistente = asistenciaOpt.get();
                asistenciaExistente.setHoraSalida(horaActual);
                asistenciaExistente.setEventoSalida(nuevoEvento);
                asistenciaRepository.save(asistenciaExistente);
            } else {
                Asistencia asistenciaInconsistente = new Asistencia();
                asistenciaInconsistente.setPersonal(personal);
                asistenciaInconsistente.setFecha(hoy);
                asistenciaInconsistente.setHoraSalida(horaActual);
                asistenciaInconsistente.setEventoSalida(nuevoEvento);
                asistenciaInconsistente.setEstado(AsistenciaEstado.ausente);
                asistenciaRepository.save(asistenciaInconsistente);
            }
        }

        String mensajeFeedback = "Registro de " + proximoTipoEvento + " procesado correctamente.";
        return eventoAccesoMapper.toResponseDto(nuevoEvento, mensajeFeedback);
    }
}