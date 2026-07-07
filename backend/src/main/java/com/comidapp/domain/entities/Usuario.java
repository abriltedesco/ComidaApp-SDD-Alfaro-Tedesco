package com.comidapp.domain.entities;

import com.comidapp.domain.enums.RolUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Entidad base de usuario con herencia SINGLE_TABLE.
 * Migrada de TP3 com.example.demo.entity.Usuario.
 * Discriminador "tipo" → enum RolUsuario.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public class Usuario {

    @Id
    @Column(name = "dni", nullable = false)
    private int dni;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "mail", nullable = false, unique = true)
    private String mail;

    @JsonIgnore
    @Column(name = "contrasenia", nullable = false)
    private String contrasenia;

    @Column(name = "telefono")
    private int telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", insertable = false, updatable = false)
    private RolUsuario tipo;

    // ── Getters / Setters (migrados de TP3) ────────────────────────────────

    public int getDni() { return dni; }
    public void setDni(int dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }

    public int getTelefono() { return telefono; }
    public void setTelefono(int telefono) { this.telefono = telefono; }

    public RolUsuario getTipo() { return tipo; }
    public void setTipo(RolUsuario tipo) { this.tipo = tipo; }
}
