package com.comidapp.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.comidapp.domain.enums.RolUsuario;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

/**
 * UserDetailsService — migrado de TP3 com.example.demo.service.UserDetailsServiceImpl.
 * Asigna roles ROLE_ADMIN, ROLE_REPARTIDOR, ROLE_CLIENTE según el tipo de usuario.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        return usuarioRepository.findByMail(mail)
                .map(u -> {
                    String role = u.getTipo() != null ? u.getTipo().name() : RolUsuario.CLIENTE.name();
                    return org.springframework.security.core.userdetails.User
                            .withUsername(u.getMail())
                            .password(u.getContrasenia())
                            .roles(role)
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + mail));
    }
}
