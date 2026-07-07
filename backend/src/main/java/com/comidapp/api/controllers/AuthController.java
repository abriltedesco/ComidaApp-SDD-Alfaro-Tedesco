package com.comidapp.api.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.api.dto.RegistroClienteDTO;
import com.comidapp.application.auth.AutenticacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller de autenticación — migrado de TP3 AuthController.
 * Registro de cliente y login con JWT.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro y login de usuarios")
public class AuthController {

    @Autowired
    private AutenticacionService autenticacionService;

    @Operation(summary = "Registrar un cliente")
    @PostMapping("/registro/cliente")
    public ResponseEntity<Map<String, String>> registrarCliente(@Valid @RequestBody RegistroClienteDTO dto) {
        autenticacionService.registrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Cliente registrado correctamente"));
    }

    @Operation(summary = "Login — devuelve token JWT")
    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String mail,
            @RequestParam String contrasenia) {
        String token = autenticacionService.login(mail, contrasenia);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
