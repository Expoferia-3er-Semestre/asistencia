package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.entity.*;
import com.supertesis.asistencia.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final DepartamentoRepository departamentoRepository;
    private final CargoRepository cargoRepository;
    private final RolRepository rolRepository;
    private final PersonalRepository personalRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create-test-user")
    public ResponseEntity<Map<String, Object>> createTestUser() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verificar si ya existe
            if (usuarioRepository.findByNombreUsuario("admin").isPresent()) {
                response.put("message", "Usuario de prueba ya existe");
                response.put("usuario", "admin");
                response.put("password", "admin123");
                return ResponseEntity.ok(response);
            }

            // Crear departamento
            Departamento departamento = new Departamento();
            departamento.setNombre("Recursos Humanos");
            departamento.setDescripcion("Departamento de gestión del personal");
            departamento = departamentoRepository.save(departamento);

            // Crear cargo
            Cargo cargo = new Cargo();
            cargo.setNombreCargo("Administrador");
            cargo.setDepartamento(departamento);
            cargo = cargoRepository.save(cargo);

            // Crear rol
            Rol rol = new Rol();
            rol.setNombreRol("ADMIN");
            rol.setDescripcion("Rol de administrador del sistema");
            rol = rolRepository.save(rol);

            // Crear personal
            Personal personal = new Personal();
            personal.setNombre("Admin");
            personal.setApellido("Sistema");
            personal.setCorreo("admin@sistema.com");
            personal.setCedula("1234567890");
            personal.setTelefono("0999999999");
            personal.setDepartamento(departamento);
            personal.setCargo(cargo);
            personal = personalRepository.save(personal);

            // Crear usuario
            Usuario usuario = new Usuario();
            usuario.setNombreUsuario("admin");
            usuario.setPasswordHash(passwordEncoder.encode("admin123"));
            usuario.setPersonal(personal);
            usuario.setRol(rol);
            usuario.setActivo(true);
            usuario.setCreadoEn(LocalDateTime.now());
            usuario = usuarioRepository.save(usuario);

            response.put("message", "Usuario de prueba creado exitosamente");
            response.put("usuario", "admin");
            response.put("password", "admin123");
            response.put("success", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error creando usuario de prueba: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/check-user")
    public ResponseEntity<Map<String, Object>> checkUser() {
        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = usuarioRepository.findByNombreUsuario("admin")
                    .orElse(null);

            if (usuario == null) {
                response.put("message", "Usuario 'admin' no existe");
                response.put("exists", false);
                return ResponseEntity.ok(response);
            }

            response.put("message", "Usuario encontrado");
            response.put("exists", true);
            response.put("usuario", Map.of(
                "id", usuario.getId(),
                "nombreUsuario", usuario.getNombreUsuario(),
                "passwordHash", usuario.getPasswordHash(),
                "activo", usuario.getActivo(),
                "rol", usuario.getRol() != null ? usuario.getRol().getNombreRol() : null,
                "personalId", usuario.getPersonal() != null ? usuario.getPersonal().getId() : null
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error verificando usuario: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}