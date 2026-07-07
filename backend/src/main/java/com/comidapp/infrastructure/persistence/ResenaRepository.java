package com.comidapp.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.Resena;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByProductoId(Long productoId);

    @Query("SELECT AVG(r.puntuacion) FROM Resena r WHERE r.producto.id = :productoId")
    Double findPromedioByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT r.producto.id, AVG(r.puntuacion), COUNT(r) FROM Resena r GROUP BY r.producto.id")
    List<Object[]> findPromediosAgrupados();

    /** Rating global por nombre de producto (agrega todos los locales) */
    @Query("SELECT r.producto.nombre, AVG(r.puntuacion), COUNT(r) FROM Resena r GROUP BY r.producto.nombre")
    List<Object[]> findPromediosPorNombre();

    /** Reseñas (con comentario) de todos los productos con un nombre dado */
    @Query("SELECT r FROM Resena r WHERE r.producto.nombre = :nombre ORDER BY r.fechaHora DESC")
    List<Resena> findByProductoNombre(@Param("nombre") String nombre);

    boolean existsByProductoIdAndClienteDni(Long productoId, int clienteDni);

    void deleteByProductoId(Long productoId);
}
