package com.supertesis.asistencia.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Indicar a Spring Security que use la configuración de CORS definida abajo
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Deshabilitar CSRF (común en APIs que usan JWT o para pruebas locales)
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Ajusta esto según tus necesidades de acceso
            );
            
        return http.build();
    }

    // 3. Definir la fuente de configuración de CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite el origen desde donde haces el fetch (puedes usar "*" para pruebas)
        configuration.setAllowedOrigins(List.of("*")); 
        
        // Permite los métodos HTTP necesarios
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Permite todos los encabezados (importante si usas JWT o Content-Type)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Permite enviar credenciales (cookies, auth headers) si fuera necesario
        // configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}