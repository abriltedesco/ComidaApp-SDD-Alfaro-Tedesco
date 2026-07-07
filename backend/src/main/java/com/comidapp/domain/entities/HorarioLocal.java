package com.comidapp.domain.entities;

import java.time.LocalTime;

import com.comidapp.domain.enums.DiaSemana;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Horario de apertura y cierre de un local para un día de la semana.
 */
@Entity
@Table(name = "horario_local")
public class HorarioLocal {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura;

    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre;

    public HorarioLocal() {}

    /**
     * Verifica si la hora dada cae dentro del horario de apertura.
     * Soporta horarios nocturnos donde horaCierre < horaApertura (ej: 11:00-02:00).
     */
    public boolean estaEnHorario(LocalTime hora) {
        if (horaCierre.isBefore(horaApertura)) {
            // Nocturno: abierto si hora >= apertura O hora <= cierre (del día siguiente)
            return !hora.isBefore(horaApertura) || !hora.isAfter(horaCierre);
        }
        return !hora.isBefore(horaApertura) && !hora.isAfter(horaCierre);
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }

    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalTime horaApertura) { this.horaApertura = horaApertura; }

    public LocalTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalTime horaCierre) { this.horaCierre = horaCierre; }
}
