package com.comidapp.api.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.application.pedidos.CancelarPedidoVencidoUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint manual para forzar cancelación de pedidos vencidos (admin).
 * La cancelación automática ya corre cada 5 min via @Scheduled.
 */
@RestController
@RequestMapping("/api/v1/admin/jobs")
@Tag(name = "Jobs", description = "Tareas programadas")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class JobsController {

    @Autowired
    private CancelarPedidoVencidoUseCase cancelarVencidosUseCase;

    @Operation(summary = "Forzar cancelación de pedidos vencidos")
    @PostMapping("/cancelar-vencidos")
    public ResponseEntity<Map<String, String>> forzarCancelacion() {
        cancelarVencidosUseCase.cancelarVencidos();
        return ResponseEntity.ok(Map.of("mensaje", "Cancelación de vencidos ejecutada"));
    }
}
