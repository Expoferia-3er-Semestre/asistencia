package com.supertesis.asistencia.backend;

import com.supertesis.asistencia.backend.entity.*;
import com.supertesis.asistencia.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Profile("!prod") // Se ejecuta en todos los perfiles excepto prod
public class DataInitializer implements CommandLineRunner {

    private final DepartamentoRepository departamentoRepository;
    private final CargoRepository cargoRepository;
    private final RolRepository rolRepository;
    private final PersonalRepository personalRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Solo crear datos si no existen
        if (usuarioRepository.count() == 0) {
            System.out.println("=== CREANDO USUARIO DE PRUEBA ===");

            try {
                // Crear departamento
                Departamento departamento = new Departamento();
                departamento.setNombre("Recursos Humanos");
                departamento.setDescripcion("Departamento de gestión del personal");
                departamento.setActivo(true);
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
                personal.setCorreo("reo@sistema.com");
                personal.setCedula("1234567890");
                personal.setTelefono("0999999999");
                // Crear usuario
                Usuario usuario = new Usuario();
                usuario.setNombreUsuario("admin");
                usuario.setPasswordHash(passwordEncoder.encode("admin123"));
                usuario.setPersonal(personal);
                usuario.setRol(rol);
                usuario.setActivo(true);
                usuario.setCreadoEn(LocalDateTime.now());
                usuario = usuarioRepository.save(usuario);

                System.out.println("✓ Usuario de prueba creado exitosamente");
                System.out.println("  Usuario: admin");
                System.out.println("  Contraseña: admin123");
                System.out.println("  URL: http://localhost:8080");

            } catch (Exception e) {
                System.err.println("Error creando datos de prueba: " + e.getMessage());
            }
        } else {
            System.out.println("Datos de prueba ya existen, omitiendo creación");
        }
    }
}