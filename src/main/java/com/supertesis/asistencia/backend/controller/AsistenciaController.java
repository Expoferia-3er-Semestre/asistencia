package com.supertesis.asistencia.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Indica que esta clase es un controlador API REST
@RequestMapping("/api/asistencia") // Define la ruta base para este controlador
public class AsistenciaController {

    @GetMapping("/test") // Define una ruta específica para pruebas
    public String test() {
        return "El controlador de asistencia está funcionando correctamente";
    }
}