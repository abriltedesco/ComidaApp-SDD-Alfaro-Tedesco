package com.comidapp.domain.entities;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.comidapp.domain.enums.DiaSemana;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Local físico de la cadena — 5 locales fijos (FR-004).
 * Encapsula regla estaAbierto() (FR-033).
 */
@Entity
@Table(name = "local")
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @OneToMany(mappedBy = "local", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<HorarioLocal> horarios = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "local", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();

    public Local() {}

    /**
     * Verifica si el local está abierto en este momento (FR-033).
     */
    public boolean estaAbierto() {
        return estaAbiertoEn(LocalDateTime.now());
    }

    /**
     * Verifica si el local está abierto en una fecha/hora específica.
     * Soporta horarios nocturnos: si es madrugada, también verifica el horario del día anterior.
     */
    public boolean estaAbiertoEn(LocalDateTime fechaHora) {
        DiaSemana dia = DiaSemana.fromDayOfWeek(fechaHora.getDayOfWeek());
        LocalTime hora = fechaHora.toLocalTime();

        // Verificar horario del día actual
        if (horarios.stream()
                .filter(h -> h.getDiaSemana() == dia)
                .anyMatch(h -> h.estaEnHorario(hora))) {
            return true;
        }

        // Verificar si el día anterior tenía horario nocturno que cubre la madrugada actual
        DiaSemana diaAnterior = DiaSemana.fromDayOfWeek(fechaHora.minusDays(1).getDayOfWeek());
        return horarios.stream()
                .filter(h -> h.getDiaSemana() == diaAnterior)
                .filter(h -> h.getHoraCierre().isBefore(h.getHoraApertura()))
                .anyMatch(h -> h.estaEnHorario(hora));
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public List<HorarioLocal> getHorarios() { return horarios; }
    public void setHorarios(List<HorarioLocal> horarios) { this.horarios = horarios; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }
}
