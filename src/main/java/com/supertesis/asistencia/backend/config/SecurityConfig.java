package com.supertesis.asistencia.backend.config;

import com.supertesis.asistencia.backend.security.CustomUserDetailsService;
import com.supertesis.asistencia.backend.security.JwtAuthenticationEntryPoint;
import com.supertesis.asistencia.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Importación crucial para el filtrado por métodos HTTP
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. RUTAS PÚBLICAS: Autenticación, configuración inicial y recursos estáticos
                        .requestMatchers("/api/auth/**", "/api/setup/**").permitAll()
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/modules/**", "/img/**").permitAll()
                        
                        // 2. MÓDULO QR: Solo el operador del Escáner y el Administrador pueden registrar asistencia por código QR
                        .requestMatchers("/api/asistencia/registrar-qr").hasAnyRole("ADMIN", "ESCANER")
                        
                        // 3. CONSULTAS (GET): Tanto el ADMIN como el ASISTENTE pueden visualizar listas y reportes del plantel
                        .requestMatchers(HttpMethod.GET, "/api/personal/**").hasAnyRole("ADMIN", "ASISTENTE")
                        .requestMatchers(HttpMethod.GET, "/api/departamentos/**").hasAnyRole("ADMIN", "ASISTENTE")
                        .requestMatchers(HttpMethod.GET, "/api/cargos/**").hasAnyRole("ADMIN", "ASISTENTE")
                        .requestMatchers(HttpMethod.GET, "/api/asistencia/**").hasAnyRole("ADMIN", "ASISTENTE")
                        
                        // 4. MODIFICACIONES (POST, PUT, DELETE): Exclusivo de ADMIN. El ASISTENTE o ESCANER rebotarán con 403 Forbidden
                        // 4. RESTRICCIÓN EXPLÍCITA DE MUTACIONES: Solo el Administrador puede alterar datos
                        .requestMatchers(HttpMethod.POST, "/api/personal/**", "/api/departamentos/**", "/api/cargos/**", "/api/asistencia/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/personal/**", "/api/departamentos/**", "/api/cargos/**", "/api/asistencia/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/personal/**", "/api/departamentos/**", "/api/cargos/**", "/api/asistencia/**").hasRole("ADMIN")
                        
                        // 5. CUALQUIER OTRA RUTA: Debe requerir autenticación por defecto
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // 1. Pasamos SOLO el UserDetailsService al constructor
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        
        // 2. Asignamos el PasswordEncoder a través del método setter
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Mantiene tus puertos locales del Live Server
        configuration.setAllowedOrigins(List.of("http://localhost:5500", "http://127.0.0.1:5500")); 
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}