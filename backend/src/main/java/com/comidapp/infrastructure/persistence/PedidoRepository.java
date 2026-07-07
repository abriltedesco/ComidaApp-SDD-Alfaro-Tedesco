package com.comidapp.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.Pedido;
import com.comidapp.domain.enums.EstadoPedido;

/**
 * Repositorio de pedidos — migrado de TP3 com.example.demo.repository.PedidoRepository.
 * Queries: historial, estado, repartidor, vencidos.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteDni(int dni);

    long countByClienteDni(int dni);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.cliente.dni = :dni AND p.estado <> com.comidapp.domain.enums.EstadoPedido.CANCELADO")
    long countPedidosValidosByClienteDni(@Param("dni") int dni);

    Page<Pedido> findByClienteDniOrderByFechaHoraDesc(int dni, Pageable pageable);

    List<Pedido> findByRepartidorDni(int dni);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByClienteDniAndEstado(int dni, EstadoPedido estado);

    /**
     * Pedidos vencidos: en estado PENDIENTE con más de 1 hora de antigüedad (FR-032).
     */
    @Query("SELECT p FROM Pedido p WHERE p.estado = :estado AND p.fechaHora < :limite")
    List<Pedido> findVencidos(@Param("estado") EstadoPedido estado,
                              @Param("limite") LocalDateTime limite);
}
