package com.supertesis.asistencia;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CodificarContrasenia {

    @Test
    public void generarHash() {
        System.out.println("\n========================================");
        System.out.println("HASH GENERADO: " + String.valueOf(encriptar("admin123")));
        System.out.println("========================================\n");
    }

    public String encriptar(String contrasenia){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return  encoder.encode(contrasenia);
    }
}