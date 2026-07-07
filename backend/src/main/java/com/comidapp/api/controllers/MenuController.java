package com.comidapp.api.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.api.dto.ProductoDTO;
import com.comidapp.domain.entities.Local;
import com.comidapp.domain.entities.Producto;
import com.comidapp.infrastructure.persistence.LocalRepository;
import com.comidapp.infrastructure.persistence.ProductoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller de menú — locales y productos (FR-004, FR-005, FR-006, FR-033).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Menú", description = "Locales, menú y disponibilidad")
@SecurityRequirement(name = "bearerAuth")
public class MenuController {

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Operation(summary = "Listar todos los locales")
    @GetMapping("/locales")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Local>> listarLocales() {
        return ResponseEntity.ok(localRepository.findAll());
    }

    @Operation(summary = "Obtener un local por ID")
    @GetMapping("/locales/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Local> obtenerLocal(@PathVariable Long id) {
        return localRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar productos de un local (opcionalmente filtrar por categoría)")
    @GetMapping("/locales/{localId}/menu")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductoDTO>> menuPorLocal(
            @PathVariable Long localId,
            @RequestParam(required = false) String categoria) {

        List<Producto> productos;
        if (categoria != null && !categoria.isBlank()) {
            productos = productoRepository.findByLocalIdAndCategoria(localId, categoria);
        } else {
            productos = productoRepository.findByLocalIdAndDisponibleTrue(localId);
        }

        List<ProductoDTO> dtos = productos.stream()
                .map(ProductoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Buscar productos por nombre y/o categoría (global)")
    @GetMapping("/productos/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductoDTO>> buscarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria) {

        List<Producto> productos;
        if (categoria != null && !categoria.isBlank() && nombre != null && !nombre.isBlank()) {
            productos = productoRepository.findByCategoriaAndNombreContainingIgnoreCase(categoria, nombre);
        } else if (categoria != null && !categoria.isBlank()) {
            productos = productoRepository.findByCategoria(categoria);
        } else if (nombre != null && !nombre.isBlank()) {
            productos = productoRepository.findByNombreContainingIgnoreCase(nombre);
        } else {
            productos = productoRepository.findAll();
        }

        List<ProductoDTO> dtos = productos.stream()
                .filter(Producto::isDisponible)
                .map(ProductoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Listar locales con su estado abierto/cerrado actual")
    @GetMapping("/locales/estado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> localesConEstado() {
        List<Map<String, Object>> resultado = localRepository.findAll().stream()
                .map(local -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", local.getId());
                    info.put("nombre", local.getNombre());
                    info.put("direccion", local.getDireccion());
                    info.put("telefono", local.getTelefono());
                    info.put("abierto", local.estaAbierto());
                    // Incluir horarios para el frontend
                    List<Map<String, String>> horarios = local.getHorarios().stream()
                            .map(h -> {
                                Map<String, String> hm = new HashMap<>();
                                hm.put("diaSemana", h.getDiaSemana().name());
                                hm.put("horaApertura", h.getHoraApertura().toString());
                                hm.put("horaCierre", h.getHoraCierre().toString());
                                return hm;
                            })
                            .collect(Collectors.toList());
                    info.put("horarios", horarios);
                    return info;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Listar categorías de productos disponibles")
    @GetMapping("/productos/categorias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> listarCategorias() {
        List<String> categorias = productoRepository.findAll().stream()
                .filter(Producto::isDisponible)
                .map(Producto::getCategoria)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }
}