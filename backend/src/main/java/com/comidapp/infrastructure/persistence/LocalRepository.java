package com.comidapp.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.Local;

/**
 * Repositorio de locales.
 */
@Repository
public interface LocalRepository extends JpaRepository<Local, Long> {
}
