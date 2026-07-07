package com.comidapp.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.Pago;

/**
 * Repositorio de pagos.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);

    Optional<Pago> findByMpPaymentId(String mpPaymentId);
}
