package com.comidapp.api.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.application.notificaciones.EnviarNotificacionUseCase;
import com.comidapp.domain.entities.SuscripcionPush;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller de notificaciones push — suscripción y envío (FR-018).
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
@Tag(name = "Notificaciones", description = "Web Push Notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificacionController {

    @Autowired
    private EnviarNotificacionUseCase notificacionUseCase;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Suscribirse a notificaciones push")
    @PostMapping("/suscribir")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> suscribir(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        int dni = extraerDni(auth);
        String endpoint = body.get("endpoint");
        String p256dh = body.get("p256dh");
        String authKey = body.get("auth");

        SuscripcionPush suscripcion = notificacionUseCase.suscribir(dni, endpoint, p256dh, authKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", suscripcion.getId(),
                "mensaje", "Suscripción registrada"
        ));
    }

    @Operation(summary = "Cancelar suscripción push")
    @DeleteMapping("/suscribir/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> desuscribir(@PathVariable Long id) {
        notificacionUseCase.desuscribir(id);
        return ResponseEntity.ok(Map.of("mensaje", "Suscripción eliminada"));
    }

    @Operation(summary = "Enviar notificación a un usuario (admin)")
    @PostMapping("/enviar/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> enviar(
            @PathVariable int dni,
            @RequestBody Map<String, String> body) {
        String titulo = body.getOrDefault("titulo", "ComidApp");
        String mensaje = body.getOrDefault("mensaje", "");
        notificacionUseCase.notificarUsuario(dni, titulo, mensaje);
        return ResponseEntity.ok(Map.of("mensaje", "Notificación enviada"));
    }

    private int extraerDni(Authentication auth) {
        String mail = auth.getName();
        return usuarioRepository.findByMail(mail).map(u -> u.getDni()).orElse(0);
    }
}
