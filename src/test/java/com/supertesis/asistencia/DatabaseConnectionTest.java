package com.supertesis.asistencia; // Asegúrate de usar tu paquete real

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // <-- CRITICAL: Esto le dice a Spring que use application-test.properties
class DatabaseConnectionTest {

    @Test
    void contextLoads() {
        // Si este método se ejecuta sin errores, tu configuración de H2 es exitosa
        System.out.println("¡Conexión a H2 exitosa! El contexto de Spring arrancó correctamente.");
    }
}