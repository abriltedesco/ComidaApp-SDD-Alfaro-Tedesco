package com.comidapp.domain.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Admin — usuario administrador del sistema.
 * Migrada de TP3 com.example.demo.entity.Admin.
 */
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Usuario {
}
