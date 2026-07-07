package com.comidapp.api.dto;

import com.comidapp.domain.entities.Producto;

/**
 * DTO de respuesta de producto — migrado de TP3 ProductoPedidoDTO.
 */
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precioUnitario;
    private String categoria;
    private String imagenUrl;
    private boolean disponible;
    private Long localId;
    private String localNombre;
    private Double rating;
    private Integer totalResenas;

    public ProductoDTO(Producto p) {
        this.id = p.getId();
        this.nombre = p.getNombre();
        this.descripcion = p.getDescripcion();
        this.precioUnitario = p.getPrecioUnitario();
        this.categoria = p.getCategoria();
        this.imagenUrl = p.getImagenUrl();
        this.disponible = p.isDisponible();
        if (p.getLocal() != null) {
            this.localId = p.getLocal().getId();
            this.localNombre = p.getLocal().getNombre();
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public String getCategoria() { return categoria; }
    public String getImagenUrl() { return imagenUrl; }
    public boolean isDisponible() { return disponible; }
    public Long getLocalId() { return localId; }
    public String getLocalNombre() { return localNombre; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getTotalResenas() { return totalResenas; }
    public void setTotalResenas(Integer totalResenas) { this.totalResenas = totalResenas; }
}
