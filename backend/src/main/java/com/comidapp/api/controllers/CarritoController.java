package com.comidapp.api.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.api.dto.AgregarItemCarritoDTO;
import com.comidapp.application.carrito.GestionarCarritoUseCase;
import com.comidapp.domain.entities.Carrito;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller de carrito — CRUD (FR-007 a FR-010).
 */
@RestController
@RequestMapping("/api/v1/carrito")
@Tag(name = "Carrito", description = "Gestión del carrito de compras")
@SecurityRequirement(name = "bearerAuth")
public class CarritoController {

    @Autowired
    private GestionarCarritoUseCase carritoUseCase;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Obtener carrito del usuario autenticado")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Carrito> obtener(Authentication auth) {
        int dni = extraerDni(auth);
        return ResponseEntity.ok(carritoUseCase.obtenerCarrito(dni));
    }

    @Operation(summary = "Agregar item al carrito")
    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Carrito> agregarItem(
            @Valid @RequestBody AgregarItemCarritoDTO dto,
            Authentication auth) {
        int dni = extraerDni(auth);
        return ResponseEntity.ok(carritoUseCase.agregarItem(dni, dto.getProductoId(), dto.getCantidad()));
    }

    @Operation(summary = "Modificar cantidad de un item")
    @PutMapping("/items/{productoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Carrito> modificarCantidad(
            @PathVariable Long productoId,
            @RequestBody Map<String, Integer> body,
            Authentication auth) {
        int dni = extraerDni(auth);
        int cantidad = body.getOrDefault("cantidad", 1);
        return ResponseEntity.ok(carritoUseCase.modificarCantidad(dni, productoId, cantidad));
    }

    @Operation(summary = "Eliminar item del carrito")
    @DeleteMapping("/items/{productoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Carrito> eliminarItem(@PathVariable Long productoId, Authentication auth) {
        int dni = extraerDni(auth);
        return ResponseEntity.ok(carritoUseCase.eliminarItem(dni, productoId));
    }

    @Operation(summary = "Vaciar el carrito completo")
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Carrito> vaciar(Authentication auth) {
        int dni = extraerDni(auth);
        return ResponseEntity.ok(carritoUseCase.vaciarCarrito(dni));
    }

    private int extraerDni(Authentication auth) {
        String mail = auth.getName();
        var usuario = usuarioRepository.findByMail(mail);
        return usuario.map(u -> u.getDni()).orElse(0);
    }
}
