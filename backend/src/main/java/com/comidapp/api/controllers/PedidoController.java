package com.comidapp.api.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.api.dto.ConfirmarPedidoDTO;
import com.comidapp.api.dto.PedidoResponseDTO;
import com.comidapp.application.pedidos.ActualizarEstadoUseCase;
import com.comidapp.application.pedidos.ConfirmarPedidoUseCase;
import com.comidapp.domain.entities.Pedido;
import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.domain.enums.MetodoPago;
import com.comidapp.domain.enums.RolUsuario;
import com.comidapp.infrastructure.persistence.PedidoRepository;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller de pedidos — confirmar, historial, actualizar estado (FR-011 a FR-017).
 */
@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Gestión de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    @Autowired
    private ConfirmarPedidoUseCase confirmarPedidoUseCase;

    @Autowired
    private ActualizarEstadoUseCase actualizarEstadoUseCase;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Confirmar pedido desde el carrito")
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PedidoResponseDTO> confirmar(
            @RequestBody ConfirmarPedidoDTO dto,
            Authentication auth) {
        int dni = extraerDni(auth);
        String metodoStr = (dto.getMetodoPago() != null && !dto.getMetodoPago().isBlank())
                ? dto.getMetodoPago().toUpperCase() : "EFECTIVO";
        MetodoPago metodo = MetodoPago.valueOf(metodoStr);
        Pedido pedido = confirmarPedidoUseCase.confirmar(dni, metodo, dto.getDirEntrega(), dto.getCupon());
        return ResponseEntity.status(HttpStatus.CREATED).body(new PedidoResponseDTO(pedido));
    }

    @Operation(summary = "Historial de pedidos del cliente autenticado (paginado)")
    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> misPedidos(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int dni = extraerDni(auth);
        Pageable pageable = PageRequest.of(page, size);
        Page<Pedido> pedidosPage = pedidoRepository.findByClienteDniOrderByFechaHoraDesc(dni, pageable);
        List<PedidoResponseDTO> content = pedidosPage.getContent()
                .stream().map(PedidoResponseDTO::new).collect(Collectors.toList());

        Map<String, Object> response = Map.of(
                "content", content,
                "page", pedidosPage.getNumber(),
                "size", pedidosPage.getSize(),
                "totalElements", pedidosPage.getTotalElements(),
                "totalPages", pedidosPage.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Pedidos asignados al repartidor autenticado")
    @GetMapping("/repartidor")
    @PreAuthorize("hasRole('REPARTIDOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PedidoResponseDTO>> pedidosRepartidor(Authentication auth) {
        int dni = extraerDni(auth);
        List<PedidoResponseDTO> pedidos = pedidoRepository.findByRepartidorDni(dni)
                .stream().map(PedidoResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Historial y estadísticas del repartidor autenticado")
    @GetMapping("/repartidor/historial")
    @PreAuthorize("hasRole('REPARTIDOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> historialRepartidor(Authentication auth) {
        int dni = extraerDni(auth);
        List<Pedido> pedidos = pedidoRepository.findByRepartidorDni(dni);

        long entregados = pedidos.stream().filter(p -> p.getEstado() == EstadoPedido.ENTREGADO).count();
        long enCurso = pedidos.stream()
                .filter(p -> p.getEstado() != EstadoPedido.ENTREGADO && p.getEstado() != EstadoPedido.CANCELADO)
                .count();
        double totalEntregado = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .mapToDouble(p -> p.getPrecioTotal() != null ? p.getPrecioTotal() : 0.0)
                .sum();

        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalAsignados", pedidos.size());
        stats.put("entregados", entregados);
        stats.put("enCurso", enCurso);
        stats.put("montoTotalEntregado", totalEntregado);
        stats.put("pedidos", pedidos.stream().map(PedidoResponseDTO::new).collect(Collectors.toList()));
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Obtener un pedido por ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<PedidoResponseDTO> obtener(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new com.comidapp.domain.exceptions.RecursoNoEncontradoException("pedido", id));
        return ResponseEntity.ok(new PedidoResponseDTO(pedido));
    }

    @Operation(summary = "Todos los pedidos (solo admin)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        List<PedidoResponseDTO> pedidos = pedidoRepository.findAll()
                .stream().map(PedidoResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Actualizar estado de un pedido")
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','REPARTIDOR','CLIENTE')")
    @Transactional
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        int dni = extraerDni(auth);
        RolUsuario rol = extraerRol(auth);
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(body.get("estado").toUpperCase());
        Pedido pedido = actualizarEstadoUseCase.actualizar(id, nuevoEstado, dni, rol);
        return ResponseEntity.ok(new PedidoResponseDTO(pedido));
    }

    @Operation(summary = "Cancelar un pedido")
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable Long id, Authentication auth) {
        int dni = extraerDni(auth);
        RolUsuario rol = extraerRol(auth);
        Pedido pedido = actualizarEstadoUseCase.cancelar(id, dni, rol);
        return ResponseEntity.ok(new PedidoResponseDTO(pedido));
    }

    @Operation(summary = "Pedido activo del cliente (último no finalizado)")
    @GetMapping("/activo")
    @PreAuthorize("hasRole('CLIENTE')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> pedidoActivo(Authentication auth) {
        int dni = extraerDni(auth);
        var activo = pedidoRepository.findByClienteDni(dni).stream()
                .filter(p -> p.getEstado() != EstadoPedido.ENTREGADO && p.getEstado() != EstadoPedido.CANCELADO)
                .findFirst();
        return activo.isPresent()
                ? ResponseEntity.ok(new PedidoResponseDTO(activo.get()))
                : ResponseEntity.ok(Map.of("mensaje", "No tenés pedidos activos"));
    }

    private int extraerDni(Authentication auth) {
        String mail = auth.getName();
        return usuarioRepository.findByMail(mail).map(u -> u.getDni()).orElse(0);
    }

    private RolUsuario extraerRol(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return RolUsuario.valueOf(role);
    }
}
