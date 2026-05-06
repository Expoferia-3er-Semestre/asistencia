package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.dto.JwtResponse;
import com.supertesis.asistencia.backend.dto.LoginRequest;
import com.supertesis.asistencia.backend.security.JwtUtils;
import com.supertesis.asistencia.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getNombreUsuario(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    "Bearer",
                    userDetails.getUsername(),
                    userDetails.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "error", "No autorizado",
                            "message", "Usuario o contraseña incorrectos"
                    ));
        }
    }
}
