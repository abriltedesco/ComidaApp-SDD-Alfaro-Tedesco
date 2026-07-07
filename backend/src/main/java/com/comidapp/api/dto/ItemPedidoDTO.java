package com.comidapp.api.dto;

import com.comidapp.domain.entities.ItemPedido;

/**
 * DTO de item de pedido.
 */
public class ItemPedidoDTO {

    private Long productoId;
    private String nombreProducto;
    private Double precioUnitario;
    private int cantidad;

    public ItemPedidoDTO(ItemPedido item) {
        if (item.getProducto() != null) {
            this.productoId = item.getProducto().getId();
            this.nombreProducto = item.getProducto().getNombre();
        } else {
            this.productoId = null;
            this.nombreProducto = "(Producto eliminado)";
        }
        this.precioUnitario = item.getPrecioUnitario();
        this.cantidad = item.getCantidad();
    }

    public Long getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
}
