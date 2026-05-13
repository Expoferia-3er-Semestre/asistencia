package com.supertesis.asistencia.backend.controller;

import com.supertesis.asistencia.backend.entity.Personal;
import com.supertesis.asistencia.backend.service.PersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class PersonalController {

    private final PersonalService personalService;

    @GetMapping
    public ResponseEntity<List<Personal>> findAll() {
        return ResponseEntity.ok(personalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personal> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(personalService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Personal> save(@RequestBody Personal personal) {
        return ResponseEntity.ok(personalService.save(personal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Personal> update(@PathVariable Integer id, @RequestBody Personal personal) {
        return ResponseEntity.ok(personalService.update(id, personal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        personalService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}