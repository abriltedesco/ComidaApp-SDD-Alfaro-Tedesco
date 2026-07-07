package com.comidapp.api.dto;

/**
 * DTO request para confirmar pedido — migrado de TP3 PedidoRequestDTO.
 */
public class ConfirmarPedidoDTO {

    private String metodoPago;

    private String dirEntrega;

    private String cupon;

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getDirEntrega() { return dirEntrega; }
    public void setDirEntrega(String dirEntrega) { this.dirEntrega = dirEntrega; }
    public String getCupon() { return cupon; }
    public void setCupon(String cupon) { this.cupon = cupon; }
}
