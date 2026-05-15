package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.dto.cargo.CargoRequestDto;
import com.supertesis.asistencia.backend.dto.cargo.CargoResponseDto;
import com.supertesis.asistencia.backend.service.CargoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    public ResponseEntity<List<CargoResponseDto>> findAll(
            @RequestParam(required = false) Integer departamentoId) {
        
        if (departamentoId != null) {
            return ResponseEntity.ok(cargoService.findByDepartamentoId(departamentoId));
        }
        
        return ResponseEntity.ok(cargoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(cargoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CargoResponseDto> save(@Valid @RequestBody CargoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cargoService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CargoResponseDto> update(@PathVariable Integer id, @Valid @RequestBody CargoRequestDto request) {
        return ResponseEntity.ok(cargoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        cargoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}