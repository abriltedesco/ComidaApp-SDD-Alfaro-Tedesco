package com.comidapp.domain.entities;

import java.util.ArrayList;
import java.util.List;

import com.comidapp.domain.exceptions.CarritoLocalException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Carrito de compras — restricción un solo local por sesión (FR-007).
 * Lógica de dominio: calcularTotal, validarLocal, agregarItem.
 */
@Entity
@Table(name = "carrito")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_dni", nullable = false, unique = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "local_id")
    private Local local;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemCarrito> items = new ArrayList<>();

    public Carrito() {}

    // ── Lógica de dominio ──────────────────────────────────────────────────

    /**
     * Agrega un producto al carrito.
     * Valida restricción de un solo local (FR-007).
     * Si el producto ya está, incrementa cantidad (FR-007b).
     */
    public void agregarItem(Producto producto, int cantidad) {
        validarLocal(producto);

        // Si el producto ya existe en el carrito, sumar cantidad
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(producto.getId())) {
                item.setCantidad(item.getCantidad() + cantidad);
                return;
            }
        }

        // Producto nuevo
        ItemCarrito nuevoItem = new ItemCarrito(this, producto, cantidad);
        items.add(nuevoItem);

        // Fijar el local del carrito si es el primer item
        if (this.local == null) {
            this.local = producto.getLocal();
        }
    }

    /**
     * Valida que el producto pertenezca al mismo local del carrito (FR-007).
     * @throws CarritoLocalException si el local no coincide.
     */
    private void validarLocal(Producto producto) {
        if (this.local != null && producto.getLocal() != null
                && !this.local.getId().equals(producto.getLocal().getId())) {
            throw new CarritoLocalException();
        }
    }

    /**
     * Calcula el total del carrito.
     */
    public Double calcularTotal() {
        return items.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();
    }

    /**
     * Elimina un item por ID de producto (FR-009).
     */
    public void eliminarItem(Long productoId) {
        items.removeIf(item -> item.getProducto().getId().equals(productoId));
        if (items.isEmpty()) {
            this.local = null;
        }
    }

    /**
     * Modifica la cantidad de un item (FR-010).
     */
    public void modificarCantidad(Long productoId, int nuevaCantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(productoId)) {
                if (nuevaCantidad <= 0) {
                    eliminarItem(productoId);
                } else {
                    item.setCantidad(nuevaCantidad);
                }
                return;
            }
        }
    }

    /**
     * Vacía el carrito completamente (FR-007c).
     */
    public void vaciar() {
        items.clear();
        this.local = null;
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }

    public List<ItemCarrito> getItems() { return items; }
    public void setItems(List<ItemCarrito> items) { this.items = items; }
}
