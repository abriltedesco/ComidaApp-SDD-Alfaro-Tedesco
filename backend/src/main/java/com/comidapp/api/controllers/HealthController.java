package com.comidapp.api.controllers;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.infrastructure.persistence.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Health check público — estado del sistema.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Estado del sistema")
public class HealthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Verificar estado del sistema")
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean dbOk;
        try {
            usuarioRepository.count();
            dbOk = true;
        } catch (Exception e) {
            dbOk = false;
        }

        return ResponseEntity.ok(Map.of(
                "status", dbOk ? "UP" : "DOWN",
                "database", dbOk ? "MySQL comida_app conectada" : "Error de conexión",
                "timestamp", LocalDateTime.now().toString(),
                "version", "1.0.0"
        ));
    }
}
