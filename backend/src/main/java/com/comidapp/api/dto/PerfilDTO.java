package com.comidapp.api.dto;

import com.comidapp.domain.entities.Cliente;
import com.comidapp.domain.entities.Repartidor;
import com.comidapp.domain.entities.Usuario;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO de respuesta de perfil — migrado de TP3 PerfilDTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerfilDTO {

    private int dni;
    private String nombre;
    private String apellido;
    private String mail;
    private int telefono;
    private String tipo;
    private Boolean disponible;
    private String dirEntrega;
    private String ciudad;

    public PerfilDTO(Usuario usuario) {
        this.dni = usuario.getDni();
        this.nombre = usuario.getNombre();
        this.apellido = usuario.getApellido();
        this.mail = usuario.getMail();
        this.telefono = usuario.getTelefono();
        this.tipo = usuario.getTipo() != null ? usuario.getTipo().name() : null;
        if (usuario instanceof Cliente cliente) {
            this.dirEntrega = cliente.getDirEntrega();
            this.ciudad = cliente.getCiudad();
        }
        if (usuario instanceof Repartidor repartidor) {
            this.disponible = repartidor.isDisponible();
        }
    }

    public int getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getMail() { return mail; }
    public int getTelefono() { return telefono; }
    public String getTipo() { return tipo; }
    public Boolean getDisponible() { return disponible; }
    public String getDirEntrega() { return dirEntrega; }
    public String getCiudad() { return ciudad; }
}
