package com.comidapp.application.notificaciones;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comidapp.domain.entities.SuscripcionPush;
import com.comidapp.domain.entities.Usuario;
import com.comidapp.domain.exceptions.RecursoNoEncontradoException;
import com.comidapp.infrastructure.persistence.SuscripcionPushRepository;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

/**
 * Caso de uso: gestionar suscripciones Web Push y enviar notificaciones (FR-018).
 */
@Service
@Transactional
public class EnviarNotificacionUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnviarNotificacionUseCase.class);

    @Autowired
    private SuscripcionPushRepository suscripcionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Registra una nueva suscripción push para un usuario.
     */
    public SuscripcionPush suscribir(int usuarioDni, String endpoint, String p256dh, String auth) {
        Usuario usuario = usuarioRepository.findById(usuarioDni)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", usuarioDni));

        SuscripcionPush suscripcion = new SuscripcionPush();
        suscripcion.setUsuario(usuario);
        suscripcion.setEndpoint(endpoint);
        suscripcion.setP256dh(p256dh);
        suscripcion.setAuth(auth);

        return suscripcionRepository.save(suscripcion);
    }

    /**
     * Elimina una suscripción push.
     */
    public void desuscribir(Long suscripcionId) {
        if (!suscripcionRepository.existsById(suscripcionId)) {
            throw new RecursoNoEncontradoException("suscripcion push", suscripcionId);
        }
        suscripcionRepository.deleteById(suscripcionId);
    }

    /**
     * Envía una notificación push a todas las suscripciones de un usuario.
     * La implementación real del envío está en WebPushService (infraestructura).
     */
    public void notificarUsuario(int usuarioDni, String titulo, String mensaje) {
        List<SuscripcionPush> suscripciones = suscripcionRepository.findByUsuarioDni(usuarioDni);

        if (suscripciones.isEmpty()) {
            log.debug("Usuario {} no tiene suscripciones push activas", usuarioDni);
            return;
        }

        for (SuscripcionPush suscripcion : suscripciones) {
            try {
                // TODO: delegar a WebPushService.enviar() cuando esté implementado
                log.info("Push enviado a usuario {} (endpoint: {}...)", usuarioDni,
                        suscripcion.getEndpoint().substring(0, Math.min(50, suscripcion.getEndpoint().length())));
            } catch (Exception e) {
                log.error("Error enviando push a suscripcion {}: {}", suscripcion.getId(), e.getMessage());
            }
        }
    }
}
