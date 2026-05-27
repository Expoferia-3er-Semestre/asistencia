package com.supertesis.asistencia.backend.security;

import com.supertesis.asistencia.backend.entity.Personal;
import com.supertesis.asistencia.backend.exception.ResourceNotFoundException;
import com.supertesis.asistencia.backend.repository.PersonalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QrTokenService {

    @Autowired
    private PersonalRepository personalRepository;

    @Autowired
    private JwtUtils jwtUtils;

    // Ahora retorna directamente el String del token
    public String generateQrToken(Integer personalId) {
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal", personalId.toString()));

        Map<String, Object> claims = new HashMap<>();
        claims.put("tipo", "qr");
        
        String subject = String.valueOf(personal.getId()); 
        long expirationMillis = 30L * 24 * 60 * 60 * 1000;

        return jwtUtils.generateTokenWithClaims(subject, claims, expirationMillis);
    }
}