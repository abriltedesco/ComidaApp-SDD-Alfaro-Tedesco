package com.comidapp.api.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.application.pagos.ProcesarPagoUseCase;
import com.comidapp.domain.entities.Pago;
import com.comidapp.domain.enums.EstadoPago;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller de pagos — webhook MP + confirmar efectivo (FR-014 a FR-016).
 */
@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "Procesamiento de pagos")
public class PagoController {

    @Autowired
    private ProcesarPagoUseCase procesarPagoUseCase;

    @Operation(summary = "Iniciar pago para un pedido")
    @PostMapping("/{pedidoId}/iniciar")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> iniciarPago(@PathVariable Long pedidoId) {
        Pago pago = procesarPagoUseCase.iniciarPago(pedidoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "pagoId", pago.getId(),
                "monto", pago.getMonto(),
                "estado", pago.getEstado().name()
        ));
    }

    @Operation(summary = "Webhook de Mercado Pago (público)")
    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<Map<String, String>> webhookMP(@RequestBody Map<String, Object> payload) {
        // Extraer datos del webhook de MP
        String mpPaymentId = String.valueOf(payload.getOrDefault("id", ""));
        String status = String.valueOf(payload.getOrDefault("status", ""));

        EstadoPago estadoPago = switch (status) {
            case "approved" -> EstadoPago.APROBADO;
            case "rejected" -> EstadoPago.RECHAZADO;
            default -> EstadoPago.PENDIENTE;
        };

        procesarPagoUseCase.procesarWebhookMP(mpPaymentId, estadoPago);
        return ResponseEntity.ok(Map.of("mensaje", "Webhook procesado"));
    }

    @Operation(summary = "Confirmar pago en efectivo (repartidor)")
    @PostMapping("/{pedidoId}/confirmar-efectivo")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('REPARTIDOR','ADMIN')")
    public ResponseEntity<Map<String, Object>> confirmarEfectivo(@PathVariable Long pedidoId) {
        Pago pago = procesarPagoUseCase.confirmarPagoEfectivo(pedidoId);
        return ResponseEntity.ok(Map.of(
                "pagoId", pago.getId(),
                "monto", pago.getMonto(),
                "estado", pago.getEstado().name(),
                "mensaje", "Pago en efectivo confirmado"
        ));
    }
}
