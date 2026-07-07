package com.comidapp.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.SuscripcionPush;

/**
 * Repositorio de suscripciones Web Push.
 */
@Repository
public interface SuscripcionPushRepository extends JpaRepository<SuscripcionPush, Long> {

    List<SuscripcionPush> findByUsuarioDni(int usuarioDni);
}
