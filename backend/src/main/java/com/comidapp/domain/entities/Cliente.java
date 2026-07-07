package com.comidapp.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Cliente — usuario que realiza pedidos.
 * Migrada de TP3 com.example.demo.entity.Cliente.
 */
@Entity
@DiscriminatorValue("CLIENTE")
public class Cliente extends Usuario {

    @Column(name = "dir_entrega")
    private String dirEntrega;

    @Column(name = "ciudad")
    private String ciudad;

    public String getDirEntrega() { return dirEntrega; }
    public void setDirEntrega(String dirEntrega) { this.dirEntrega = dirEntrega; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
}
