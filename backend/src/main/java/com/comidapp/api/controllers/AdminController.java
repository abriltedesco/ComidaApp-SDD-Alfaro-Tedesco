package com.comidapp.api.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.api.dto.PedidoResponseDTO;
import com.comidapp.api.dto.PerfilDTO;
import com.comidapp.api.dto.ProductoRequestDTO;
import com.comidapp.api.dto.RegistroRepartidorDTO;
import com.comidapp.application.auth.AutenticacionService;
import com.comidapp.domain.entities.Local;
import com.comidapp.domain.entities.Pedido;
import com.comidapp.domain.entities.Producto;
import com.comidapp.domain.entities.Usuario;
import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.domain.enums.RolUsuario;
import com.comidapp.domain.exceptions.RecursoNoEncontradoException;
import com.comidapp.infrastructure.persistence.ItemCarritoRepository;
import com.comidapp.infrastructure.persistence.LocalRepository;
import com.comidapp.infrastructure.persistence.PedidoRepository;
import com.comidapp.infrastructure.persistence.ProductoRepository;
import com.comidapp.infrastructure.persistence.ResenaRepository;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller de administración — gestión de productos, usuarios y locales (FR-019 a FR-026).
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administración", description = "Panel de administración")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private AutenticacionService autenticacionService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    // === PRODUCTOS ===

    @Operation(summary = "Listar todos los productos")
    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoRepository.findAll());
    }

    @Operation(summary = "Crear un producto")
    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody ProductoRequestDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setCategoria(dto.getCategoria());
        producto.setImagenUrl(dto.getImagenUrl());
        producto.setDisponible(true);

        if (dto.getLocalId() != null) {
            Local local = localRepository.findById(dto.getLocalId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("local", dto.getLocalId()));
            producto.setLocal(local);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(productoRepository.save(producto));
    }

    @Operation(summary = "Modificar un producto")
    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> modificarProducto(
            @PathVariable Long id,
            @RequestBody ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("producto", id));

        if (dto.getNombre() != null) producto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) producto.setDescripcion(dto.getDescripcion());
        if (dto.getPrecioUnitario() != null) producto.setPrecioUnitario(dto.getPrecioUnitario());
        if (dto.getCategoria() != null) producto.setCategoria(dto.getCategoria());
        if (dto.getImagenUrl() != null) producto.setImagenUrl(dto.getImagenUrl());

        return ResponseEntity.ok(productoRepository.save(producto));
    }

    @Operation(summary = "Eliminar un producto")
    @DeleteMapping("/productos/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> eliminarProducto(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("producto", id);
        }
        // Eliminar dependencias antes de borrar el producto
        resenaRepository.deleteByProductoId(id);
        itemCarritoRepository.deleteByProductoId(id);
        productoRepository.desvincularDeItemsPedido(id);
        productoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado"));
    }

    @Operation(summary = "Cambiar disponibilidad de un producto")
    @PutMapping("/productos/{id}/disponibilidad")
    public ResponseEntity<Producto> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("producto", id));
        producto.setDisponible(body.getOrDefault("disponible", true));
        return ResponseEntity.ok(productoRepository.save(producto));
    }

    // === USUARIOS ===

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping("/usuarios")
    public ResponseEntity<List<PerfilDTO>> listarUsuarios() {
        List<PerfilDTO> usuarios = usuarioRepository.findAll()
                .stream().map(PerfilDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Listar usuarios por rol")
    @GetMapping("/usuarios/rol/{rol}")
    public ResponseEntity<List<PerfilDTO>> listarPorRol(@PathVariable String rol) {
        RolUsuario rolEnum = RolUsuario.valueOf(rol.toUpperCase());
        List<PerfilDTO> usuarios = usuarioRepository.findByTipo(rolEnum)
                .stream().map(PerfilDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Registrar un repartidor")
    @PostMapping("/repartidores")
    public ResponseEntity<Map<String, String>> registrarRepartidor(
            @Valid @RequestBody RegistroRepartidorDTO dto) {
        autenticacionService.registrarRepartidor(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Repartidor registrado correctamente"));
    }

    @Operation(summary = "Eliminar un usuario")
    @DeleteMapping("/usuarios/{dni}")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable int dni) {
        Usuario usuario = usuarioRepository.findById(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", dni));
        usuarioRepository.delete(usuario);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado"));
    }

    // === ESTADÍSTICAS ===

    @Operation(summary = "Obtener estadísticas generales del sistema")
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsuarios", usuarioRepository.count());
        stats.put("totalProductos", productoRepository.count());
        stats.put("totalLocales", localRepository.count());

        List<Pedido> pedidos = pedidoRepository.findAll();
        stats.put("totalPedidos", pedidos.size());

        // Pedidos por estado
        Map<String, Long> porEstado = pedidos.stream()
                .collect(Collectors.groupingBy(p -> p.getEstado().name(), Collectors.counting()));
        stats.put("pedidosPorEstado", porEstado);

        // Ingresos totales (pedidos entregados)
        double ingresos = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .mapToDouble(Pedido::getPrecioTotal)
                .sum();
        stats.put("ingresosTotales", ingresos);

        return ResponseEntity.ok(stats);
    }

    // === PEDIDOS ADMIN ===

    @Operation(summary = "Listar pedidos con filtro por estado")
    @GetMapping("/pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos(
            @RequestParam(required = false) String estado) {
        List<Pedido> pedidos;
        if (estado != null && !estado.isBlank()) {
            EstadoPedido estadoEnum = EstadoPedido.valueOf(estado.toUpperCase());
            pedidos = pedidoRepository.findByEstado(estadoEnum);
        } else {
            pedidos = pedidoRepository.findAll();
        }
        List<PedidoResponseDTO> dtos = pedidos.stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
