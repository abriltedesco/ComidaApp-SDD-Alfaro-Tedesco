package com.comidapp.api.dto;

import jakarta.validation.constraints.Positive;

/**
 * DTO de registro de repartidor — migrado de TP3 RepartidorDTO.
 */
public class RegistroRepartidorDTO {

    @Positive(message = "El DNI debe ser un número positivo")
    private int dni;

    private String nombre;
    private String apellido;
    private String mail;
    private int telefono;
    private boolean disponible = true;

    public int getDni() { return dni; }
    public void setDni(int dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public int getTelefono() { return telefono; }
    public void setTelefono(int telefono) { this.telefono = telefono; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
