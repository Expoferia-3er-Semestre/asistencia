package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.dto.AuthResponseDto;
import com.supertesis.asistencia.backend.dto.LoginRequest;
import com.supertesis.asistencia.backend.entity.Usuario;
import com.supertesis.asistencia.backend.mapper.UsuarioMapper;
import com.supertesis.asistencia.backend.repository.UsuarioRepository;
import com.supertesis.asistencia.backend.security.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final JwtUtils jwtUtils;

    @Transactional(readOnly = true)
    public AuthResponseDto iniciarSesion(LoginRequest loginRequest, HttpServletResponse response) {
        // 1. Autenticar credenciales usando el manager de Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.nombreUsuario(), loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Generar el token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 3. Construir e inyectar la cookie HttpOnly de forma segura
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwt)
                .path("/")
                .maxAge(24 * 60 * 60) // Expiración de 1 día
                .httpOnly(true)
                .secure(false)        // Cambiar a true en producción cuando uses HTTPS
                .sameSite("Lax")
                .build();
        
        response.addHeader("Set-Cookie", jwtCookie.toString());

        // 4. Recuperar la entidad mapeando el resultado con MapStruct
        Usuario usuario = usuarioRepository.findByNombreUsuario(loginRequest.nombreUsuario())
                .orElseThrow(() -> new RuntimeException("Error al recuperar el usuario autenticado"));

        return usuarioMapper.toAuthResponseDto(usuario, "Autenticación exitosa");
    }

    @Transactional(readOnly = true)
    public AuthResponseDto obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("No existe una sesión activa");
        }

        Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return usuarioMapper.toAuthResponseDto(usuario, "Sesión activa");
    }

    public void cerrarSesion(HttpServletResponse response) {
        // Limpiamos la cookie seteando su tiempo de vida a cero
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", jwtCookie.toString());
        SecurityContextHolder.clearContext();
    }
}