package com.comidapp.api.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.api.dto.PerfilDTO;
import com.comidapp.domain.entities.Cliente;
import com.comidapp.domain.entities.Repartidor;
import com.comidapp.domain.entities.Usuario;
import com.comidapp.domain.exceptions.RecursoNoEncontradoException;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller de perfil de usuario — ver y editar datos propios (FR-019, FR-031).
 */
@RestController
@RequestMapping("/api/v1/perfil")
@Tag(name = "Perfil", description = "Gestión del perfil de usuario")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Obtener perfil del usuario autenticado")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PerfilDTO> obtenerPerfil(Authentication auth) {
        String mail = auth.getName();
        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", mail));
        return ResponseEntity.ok(new PerfilDTO(usuario));
    }

    @Operation(summary = "Actualizar perfil del usuario autenticado")
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PerfilDTO> actualizarPerfil(
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        String mail = auth.getName();
        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", mail));

        // Campos editables
        if (body.containsKey("nombre")) usuario.setNombre((String) body.get("nombre"));
        if (body.containsKey("apellido")) usuario.setApellido((String) body.get("apellido"));
        if (body.containsKey("telefono")) usuario.setTelefono((Integer) body.get("telefono"));

        // Campos específicos por tipo
        if (usuario instanceof Cliente cliente) {
            if (body.containsKey("dirEntrega")) cliente.setDirEntrega((String) body.get("dirEntrega"));
            if (body.containsKey("ciudad")) cliente.setCiudad((String) body.get("ciudad"));
        }
        if (usuario instanceof Repartidor repartidor) {
            if (body.containsKey("disponible")) repartidor.setDisponible((Boolean) body.get("disponible"));
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new PerfilDTO(usuario));
    }

    @Operation(summary = "Cambiar contraseña del usuario autenticado")
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String mail = auth.getName();
        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", mail));

        String actual = body.get("passwordActual");
        String nueva = body.get("passwordNueva");

        if (actual == null || nueva == null || nueva.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La nueva contraseña debe tener al menos 6 caracteres"));
        }

        if (!passwordEncoder.matches(actual, usuario.getContrasenia())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Contraseña actual incorrecta"));
        }

        usuario.setContrasenia(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
    }

    @Operation(summary = "Cambiar disponibilidad del repartidor autenticado")
    @PutMapping("/disponibilidad")
    @PreAuthorize("hasRole('REPARTIDOR')")
    public ResponseEntity<Map<String, Object>> cambiarDisponibilidad(
            @RequestBody Map<String, Boolean> body,
            Authentication auth) {
        String mail = auth.getName();
        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", mail));

        if (usuario instanceof Repartidor repartidor) {
            boolean disponible = body.getOrDefault("disponible", true);
            repartidor.setDisponible(disponible);
            usuarioRepository.save(repartidor);
            return ResponseEntity.ok(Map.of(
                    "mensaje", disponible ? "Ahora estás disponible" : "Ya no estás disponible",
                    "disponible", disponible));
        }
        return ResponseEntity.badRequest().body(Map.of("error", (Object) "No sos repartidor"));
    }
}
