package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.dto.AuthResponseDto;
import com.supertesis.asistencia.backend.dto.LoginRequest;
import com.supertesis.asistencia.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.iniciarSesion(loginRequest, response);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> obtenerUsuarioActual() {
        try {
            AuthResponseDto authResponse = authService.obtenerUsuarioActual();
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.cerrarSesion(response);
        return ResponseEntity.ok().build();
    }
}