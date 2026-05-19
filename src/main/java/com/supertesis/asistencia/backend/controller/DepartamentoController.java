package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.dto.departamento.DepartamentoRequestDto;
import com.supertesis.asistencia.backend.dto.departamento.DepartamentoResponseDto;
import com.supertesis.asistencia.backend.service.DepartamentoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService deptoService;

    @GetMapping
    public ResponseEntity<List<DepartamentoResponseDto>> findAll() {
        return ResponseEntity.ok(deptoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartamentoResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(deptoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DepartamentoResponseDto> save(@Valid @RequestBody DepartamentoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deptoService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartamentoResponseDto> update(@PathVariable Integer id, @Valid @RequestBody DepartamentoRequestDto request) {
        return ResponseEntity.ok(deptoService.update(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        deptoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Integer id) {
        deptoService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activos")
    public ResponseEntity<List<DepartamentoResponseDto>> findActive() {
        return ResponseEntity.ok(deptoService.findActive());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        deptoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}