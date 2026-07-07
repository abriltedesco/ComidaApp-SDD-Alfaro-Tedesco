package com.comidapp.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Repartidor — usuario que entrega pedidos.
 * Migrada de TP3 com.example.demo.entity.Repartidor.
 */
@Entity
@DiscriminatorValue("REPARTIDOR")
public class Repartidor extends Usuario {

    @Column(name = "disponible")
    private boolean disponible;

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
