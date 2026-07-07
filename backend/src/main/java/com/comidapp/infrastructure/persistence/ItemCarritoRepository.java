package com.comidapp.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.ItemCarrito;

/**
 * Repositorio de ítems del carrito.
 */
@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    void deleteByProductoId(Long productoId);
}
