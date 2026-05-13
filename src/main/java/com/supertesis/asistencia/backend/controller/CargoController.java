package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.entity.Cargo;
import com.supertesis.asistencia.backend.service.CargoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    public ResponseEntity<List<Cargo>> findAll() {
        return ResponseEntity.ok(cargoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cargo> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(cargoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Cargo> save(@RequestBody Cargo cargo) {
        return ResponseEntity.ok(cargoService.save(cargo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cargo> update(@PathVariable Integer id, @RequestBody Cargo cargo) {
        return ResponseEntity.ok(cargoService.update(id, cargo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        cargoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}