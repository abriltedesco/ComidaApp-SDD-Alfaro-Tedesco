package com.comidapp.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.comidapp.domain.entities.Pedido;

/**
 * DTO de respuesta de pedido — migrado de TP3 PedidoResponseDTO.
 */
public class PedidoResponseDTO {

    private Long id;
    private String estado;
    private String estadoPago;
    private LocalDateTime fechaHora;
    private Double precioTotal;
    private String metodoPago;
    private int clienteDni;
    private String clienteNombre;
    private String clienteApellido;
    private Integer repartidorDni;
    private String repartidorNombre;
    private String dirEntrega;
    private String localNombre;
    private String localDireccion;
    private List<ItemPedidoDTO> items;

    public PedidoResponseDTO(Pedido p) {
        this.id = p.getId();
        this.estado = p.getEstado() != null ? p.getEstado().name() : null;
        this.estadoPago = p.getEstadoPago() != null ? p.getEstadoPago().name() : null;
        this.fechaHora = p.getFechaHora();
        this.precioTotal = p.getPrecioTotal();
        this.metodoPago = p.getMetodoPago() != null ? p.getMetodoPago().name() : null;
        this.clienteDni = p.getCliente().getDni();
        this.clienteNombre = p.getCliente().getNombre();
        this.clienteApellido = p.getCliente().getApellido();
        this.dirEntrega = p.getDirEntrega();
        if (p.getLocal() != null) {
            this.localNombre = p.getLocal().getNombre();
            this.localDireccion = p.getLocal().getDireccion();
        }
        if (p.getRepartidor() != null) {
            this.repartidorDni = p.getRepartidor().getDni();
            this.repartidorNombre = p.getRepartidor().getNombre();
        }
        this.items = p.getItems() != null
                ? p.getItems().stream().map(ItemPedidoDTO::new).collect(Collectors.toList())
                : List.of();
    }

    public Long getId() { return id; }
    public String getEstado() { return estado; }
    public String getEstadoPago() { return estadoPago; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Double getPrecioTotal() { return precioTotal; }
    public String getMetodoPago() { return metodoPago; }
    public int getClienteDni() { return clienteDni; }
    public String getClienteNombre() { return clienteNombre; }
    public String getClienteApellido() { return clienteApellido; }
    public Integer getRepartidorDni() { return repartidorDni; }
    public String getRepartidorNombre() { return repartidorNombre; }
    public String getDirEntrega() { return dirEntrega; }
    public String getLocalNombre() { return localNombre; }
    public String getLocalDireccion() { return localDireccion; }
    public List<ItemPedidoDTO> getItems() { return items; }
}
