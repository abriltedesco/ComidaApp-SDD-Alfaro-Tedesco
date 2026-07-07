package com.comidapp.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.Producto;

/**
 * Repositorio de productos — migrado de TP3 com.example.demo.repository.ProductoRepository.
 * Queries por categoría, local y disponibilidad.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByLocalId(Long localId);

    List<Producto> findByLocalIdAndDisponibleTrue(Long localId);

    List<Producto> findByLocalIdAndCategoria(Long localId, String categoria);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByCategoriaAndNombreContainingIgnoreCase(String categoria, String nombre);

    /** Desvincular producto de items de pedido históricos antes de eliminarlo */
    @Modifying
    @Query("UPDATE ItemPedido ip SET ip.producto = null WHERE ip.producto.id = :productoId")
    void desvincularDeItemsPedido(@Param("productoId") Long productoId);
}
