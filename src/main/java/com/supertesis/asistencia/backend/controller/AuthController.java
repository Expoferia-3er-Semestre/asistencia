package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.dto.LoginRequest;
import com.supertesis.asistencia.backend.security.JwtUtils;
import com.supertesis.asistencia.backend.security.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getNombreUsuario(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            /* Cookie httpOnly — JS no puede leerla ni robarla */
            Cookie cookie = new Cookie("jwt", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 24 horas
            // cookie.setSecure(true); // activar en producción con HTTPS
            response.addCookie(cookie);

            /* Solo devolvemos el nombre — el token ya no va en el body */
            return ResponseEntity.ok(Map.of(
                    "nombreUsuario", userDetails.getUsername(),
                    "roles", userDetails.getAuthorities().stream()
                            .map(a -> a.getAuthority()).toList()
            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "No autorizado",
                    "message", "Usuario o contraseña incorrectos"
            ));
        }
    }

    /* Verifica si la cookie es válida — lo usa verificarSesion() del frontend */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        return ResponseEntity.ok(Map.of("nombreUsuario", authentication.getName()));
    }

    /* Limpia la cookie — lo usa logout() del frontend */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // borra la cookie
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
    }
}