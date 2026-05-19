package com.supertesis.asistencia.backend.security;

import com.supertesis.asistencia.backend.entity.Personal;
import com.supertesis.asistencia.backend.repository.PersonalRepository;
import com.supertesis.asistencia.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class QrTokenService {

    @Autowired
    private PersonalRepository personalRepository;

    @Autowired
    private JwtUtils jwtUtils;

    public Map<String, Object> generateQrToken(Integer personalId) {
        // 1. Verificar si el personal existe, si no, lanzar 404
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personal no encontrado"));

        // 2. Preparar los claims requeridos
        Map<String, Object> claims = new HashMap<>();
        claims.put("tipo", "qr");
        // El sub (subject) se suele pasar como String en JWT
        String subject = String.valueOf(personalId); 

        // 3. Calcular expiración de 30 días en milisegundos (usando la L al final para forzar long)
        long expirationMillis = 30L * 24 * 60 * 60 * 1000;

        // 4. Generar el token
        String token = jwtUtils.generateTokenWithClaims(subject, claims, expirationMillis);

        // 5. Construir la respuesta con el formato solicitado
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("personalId", personal.getId());
        response.put("nombre", personal.getNombre()); // Ajusta según el campo de tu entidad (ej. getNombre Completo)

        return response;
    }
}