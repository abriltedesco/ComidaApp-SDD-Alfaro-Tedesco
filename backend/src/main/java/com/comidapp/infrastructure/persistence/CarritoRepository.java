package com.comidapp.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.Carrito;

/**
 * Repositorio de carritos — un carrito por cliente (FR-007).
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByClienteDni(int clienteDni);

    boolean existsByClienteDni(int clienteDni);
}
