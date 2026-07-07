package com.comidapp.application.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comidapp.api.dto.RegistroClienteDTO;
import com.comidapp.api.dto.RegistroRepartidorDTO;
import com.comidapp.domain.entities.Cliente;
import com.comidapp.domain.entities.Repartidor;
import com.comidapp.domain.entities.Usuario;
import com.comidapp.domain.exceptions.ConflictoException;
import com.comidapp.domain.exceptions.CredencialesInvalidasException;
import com.comidapp.infrastructure.persistence.UsuarioRepository;
import com.comidapp.infrastructure.security.JwtService;

/**
 * Servicio de autenticación — migrado de TP3 AutenticacionService.
 * Login con compatibilidad BCrypt legacy, registro de clientes y repartidores.
 */
@Service
@Transactional
public class AutenticacionService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Login — migrado de TP3 con compatibilidad de passwords legacy.
     */
    public String login(String email, String contrasenia) {
        Usuario usuario = usuarioRepository.findByMail(email)
                .orElseThrow(CredencialesInvalidasException::new);

        String passwordGuardada = usuario.getContrasenia();
        boolean credencialesValidas = false;

        // Compatibilidad: si la contraseña está en BCrypt, validar normal
        if (passwordGuardada != null && passwordGuardada.startsWith("$2a$")) {
            credencialesValidas = passwordEncoder.matches(contrasenia, passwordGuardada);
        } else if (passwordGuardada != null && passwordGuardada.equals(contrasenia)) {
            // Password legacy en texto plano — migrar a BCrypt
            credencialesValidas = true;
            usuario.setContrasenia(passwordEncoder.encode(contrasenia));
            usuarioRepository.save(usuario);
        }

        if (!credencialesValidas) {
            throw new CredencialesInvalidasException();
        }

        return jwtService.generarToken(usuario.getMail(), usuario.getTipo(), usuario.getDni());
    }

    /**
     * Registro de cliente.
     */
    public void registrarCliente(RegistroClienteDTO dto) {
        validarDuplicados(dto.getDni(), dto.getMail());

        Cliente cliente = new Cliente();
        cliente.setDni(dto.getDni());
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setMail(dto.getMail());
        cliente.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
        cliente.setTelefono(dto.getTelefono());
        cliente.setDirEntrega(dto.getDirEntrega());
        cliente.setCiudad(dto.getCiudad());

        usuarioRepository.save(cliente);
    }

    /**
     * Registro de repartidor (solo admin).
     */
    public void registrarRepartidor(RegistroRepartidorDTO dto) {
        validarDuplicados(dto.getDni(), dto.getMail());

        Repartidor repartidor = new Repartidor();
        repartidor.setDni(dto.getDni());
        repartidor.setNombre(dto.getNombre());
        repartidor.setApellido(dto.getApellido());
        repartidor.setMail(dto.getMail());
        repartidor.setContrasenia(passwordEncoder.encode("repartidor123"));
        repartidor.setTelefono(dto.getTelefono());
        repartidor.setDisponible(dto.isDisponible());

        usuarioRepository.save(repartidor);
    }

    private void validarDuplicados(int dni, String mail) {
        if (usuarioRepository.existsById(dni) || usuarioRepository.existsByMail(mail)) {
            throw new ConflictoException("Los datos ya están en uso");
        }
    }
}
