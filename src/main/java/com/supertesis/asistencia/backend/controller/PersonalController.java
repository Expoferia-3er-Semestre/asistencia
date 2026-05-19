package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.dto.personal.PersonalCreateRequestDto;
import com.supertesis.asistencia.backend.dto.personal.PersonalResponseDto;
import com.supertesis.asistencia.backend.dto.personal.PersonalUpdateRequestDto;
import com.supertesis.asistencia.backend.security.QrTokenService;
import com.supertesis.asistencia.backend.service.PersonalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class PersonalController {

    private final PersonalService personalService;
    @Autowired
    private QrTokenService qrTokenService;

    @PreAuthorize("hasRole('Administrador')") 
    @GetMapping("/{id}/qr-token")
    public ResponseEntity<Map<String, Object>> getQrToken(@PathVariable("id") Integer id) {
        Map<String, Object> response = qrTokenService.generateQrToken(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PersonalResponseDto>> findAll() {
        return ResponseEntity.ok(personalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonalResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(personalService.findById(id));
    }

    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<PersonalResponseDto> findByCedula(@PathVariable String cedula) {
        return ResponseEntity.ok(personalService.findByCedula(cedula));
    }

    @GetMapping("/correo/{correo}")
    public ResponseEntity<PersonalResponseDto> findByCorreo(@PathVariable String correo) {
        return ResponseEntity.ok(personalService.findByCorreo(correo));
    }

    @GetMapping("/departamento/{departamentoId}")
    public ResponseEntity<List<PersonalResponseDto>> findByDepartamentoId(@PathVariable Integer departamentoId) {
        return ResponseEntity.ok(personalService.findByDepartamentoId(departamentoId));
    }

    @GetMapping("/cargo/{cargoId}")
    public ResponseEntity<List<PersonalResponseDto>> findByCargoId(@PathVariable Integer cargoId) {
        return ResponseEntity.ok(personalService.findByCargoId(cargoId));
    }

    @GetMapping("/status")
    public ResponseEntity<List<PersonalResponseDto>> findByActivo(@RequestParam(name = "activo") Boolean activo) {
        return ResponseEntity.ok(personalService.findByActivo(activo));
    }
    
    @PostMapping
    public ResponseEntity<PersonalResponseDto> save(@Valid @RequestBody PersonalCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personalService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonalResponseDto> update(@PathVariable Integer id, @Valid @RequestBody PersonalUpdateRequestDto request) {
        return ResponseEntity.ok(personalService.update(id, request));
    }

    // Este elimina lógicamente (Inactivación/Despedir)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        personalService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Este reincorpora (Reactivación/Recontratar)
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivarPersonal(@PathVariable Integer id) {
        personalService.reactivar(id);
        return ResponseEntity.noContent().build();
    }

    

}