package com.comidapp.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.comidapp.domain.enums.EstadoPago;
import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.domain.enums.MetodoPago;
import com.comidapp.domain.exceptions.TransicionEstadoException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Pedido — migrada de TP3 com.example.demo.entity.Pedido.
 * Agrega reglas de dominio: puedeAvanzar, cancelar, estaVencido (FR-017, FR-017b, FR-032).
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_dni", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> items = new ArrayList<>();

    @Column(name = "precio_total", nullable = false)
    private Double precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_dni")
    private Repartidor repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "dir_entrega")
    private String dirEntrega;

    public Pedido() {}

    // ── Lógica de dominio ──────────────────────────────────────────────────

    /**
     * Verifica si el pedido puede transicionar al estado dado (FR-017).
     */
    public boolean puedeAvanzar(EstadoPedido nuevoEstado) {
        return estado.puedeTransicionarA(nuevoEstado);
    }

    /**
     * Avanza al siguiente estado validando la transición.
     * @throws TransicionEstadoException si la transición no es válida.
     */
    public void avanzarA(EstadoPedido nuevoEstado) {
        if (!puedeAvanzar(nuevoEstado)) {
            throw new TransicionEstadoException(estado.name(), nuevoEstado.name());
        }
        this.estado = nuevoEstado;
    }

    /**
     * Cancela el pedido si es posible (FR-017b).
     * @throws TransicionEstadoException si ya no se puede cancelar.
     */
    public void cancelar() {
        avanzarA(EstadoPedido.CANCELADO);
    }

    /**
     * Verifica si el pedido está vencido (más de 1 hora en PENDIENTE) (FR-032).
     */
    public boolean estaVencido() {
        if (estado != EstadoPedido.PENDIENTE || fechaHora == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(fechaHora.plusHours(1));
    }

    /**
     * Calcula el precio total sumando los ítems.
     */
    public void calcularTotal() {
        this.precioTotal = items.stream()
                .mapToDouble(item -> item.getPrecioUnitario() * item.getCantidad())
                .sum();
    }

    // ── Getters / Setters (migrados de TP3 + nuevos) ───────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public List<ItemPedido> getItems() { return items; }
    public void setItems(List<ItemPedido> items) { this.items = items; }

    public Double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(Double precioTotal) { this.precioTotal = precioTotal; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

    public Repartidor getRepartidor() { return repartidor; }
    public void setRepartidor(Repartidor repartidor) { this.repartidor = repartidor; }

    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public String getDirEntrega() { return dirEntrega; }
    public void setDirEntrega(String dirEntrega) { this.dirEntrega = dirEntrega; }
}
