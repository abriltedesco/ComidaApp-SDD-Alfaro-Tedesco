package com.comidapp.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.comidapp.domain.entities.Usuario;
import com.comidapp.domain.enums.RolUsuario;

/**
 * Repositorio de usuarios — migrado de TP3 com.example.demo.repository.UsuarioRepository.
 * Queries por email y rol.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByMail(String mail);

    List<Usuario> findByTipo(RolUsuario tipo);

    List<Usuario> findByTipoIn(List<RolUsuario> tipos);

    boolean existsByMail(String mail);
}
