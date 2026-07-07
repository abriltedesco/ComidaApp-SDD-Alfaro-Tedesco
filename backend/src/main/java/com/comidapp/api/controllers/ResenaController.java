package com.comidapp.api.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comidapp.domain.entities.Cliente;
import com.comidapp.domain.entities.Producto;
import com.comidapp.domain.entities.Resena;
import com.comidapp.infrastructure.persistence.ProductoRepository;
import com.comidapp.infrastructure.persistence.ResenaRepository;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/resenas")
@Tag(name = "Reseñas", description = "Sistema de puntuación de productos")
@SecurityRequirement(name = "bearerAuth")
public class ResenaController {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Obtener promedio de rating de un producto")
    @GetMapping("/producto/{productoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> ratingProducto(@PathVariable Long productoId) {
        Double promedio = resenaRepository.findPromedioByProductoId(productoId);
        List<Resena> resenas = resenaRepository.findByProductoId(productoId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("productoId", productoId);
        resp.put("rating", promedio != null ? Math.round(promedio * 10.0) / 10.0 : null);
        resp.put("total", resenas.size());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Obtener todos los promedios de ratings (por ID de producto)")
    @GetMapping("/promedios")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<Long, Map<String, Object>>> todosLosPromedios() {
        List<Object[]> data = resenaRepository.findPromediosAgrupados();
        Map<Long, Map<String, Object>> resultado = new HashMap<>();
        for (Object[] row : data) {
            Long prodId = (Long) row[0];
            Double avg = (Double) row[1];
            Long count = (Long) row[2];
            Map<String, Object> info = new HashMap<>();
            info.put("rating", Math.round(avg * 10.0) / 10.0);
            info.put("total", count.intValue());
            resultado.put(prodId, info);
        }
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Obtener promedios globales agrupados por nombre de producto (cross-local)")
    @GetMapping("/promedios-global")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Map<String, Object>>> promediosGlobal() {
        List<Object[]> data = resenaRepository.findPromediosPorNombre();
        Map<String, Map<String, Object>> resultado = new HashMap<>();
        for (Object[] row : data) {
            String nombre = (String) row[0];
            Double avg = (Double) row[1];
            Long count = (Long) row[2];
            Map<String, Object> info = new HashMap<>();
            info.put("rating", Math.round(avg * 10.0) / 10.0);
            info.put("total", count.intValue());
            resultado.put(nombre, info);
        }
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Obtener reseñas con comentarios de un producto (por nombre, cross-local)")
    @GetMapping("/opiniones/{nombreProducto}")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> opinionesProducto(@PathVariable String nombreProducto) {
        List<Resena> resenas = resenaRepository.findByProductoNombre(nombreProducto);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Resena r : resenas) {
            Map<String, Object> item = new HashMap<>();
            item.put("puntuacion", r.getPuntuacion());
            item.put("comentario", r.getComentario() != null ? r.getComentario() : "");
            item.put("clienteNombre", r.getCliente().getNombre() + " " + r.getCliente().getApellido());
            item.put("fecha", r.getFechaHora().toString());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Crear una reseña para un producto")
    @PostMapping("/producto/{productoId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> crearResena(
            @PathVariable Long productoId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        String mail = auth.getName();
        Cliente cliente = usuarioRepository.findByMail(mail)
                .filter(u -> u instanceof Cliente)
                .map(u -> (Cliente) u)
                .orElse(null);
        if (cliente == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cliente no encontrado"));
        }

        if (resenaRepository.existsByProductoIdAndClienteDni(productoId, cliente.getDni())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya dejaste una reseña para este producto"));
        }

        Producto producto = productoRepository.findById(productoId)
                .orElse(null);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        int puntuacion = ((Number) body.get("puntuacion")).intValue();
        if (puntuacion < 1 || puntuacion > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Puntuación debe ser entre 1 y 5"));
        }

        String comentario = (String) body.getOrDefault("comentario", "");

        Resena resena = new Resena(producto, cliente, puntuacion, comentario);
        resenaRepository.save(resena);

        return ResponseEntity.ok(Map.of(
            "mensaje", "Reseña guardada",
            "puntuacion", puntuacion
        ));
    }
}
