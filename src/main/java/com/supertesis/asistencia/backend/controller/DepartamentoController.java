package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.entity.Departamento;
import com.supertesis.asistencia.backend.service.DepartamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @GetMapping
    public ResponseEntity<List<Departamento>> findAll() {
        return ResponseEntity.ok(departamentoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departamento> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(departamentoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Departamento> save(@RequestBody Departamento departamento) {
        return ResponseEntity.ok(departamentoService.save(departamento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Departamento> update(@PathVariable Integer id, @RequestBody Departamento departamento) {
        return ResponseEntity.ok(departamentoService.update(id, departamento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        departamentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}