package com.comidapp.infrastructure.notifications;

import java.security.GeneralSecurityException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.comidapp.domain.entities.SuscripcionPush;

import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

/**
 * Servicio de infraestructura para envío de notificaciones Web Push via VAPID (FR-018).
 */
@Service
public class WebPushService {

    private static final Logger log = LoggerFactory.getLogger(WebPushService.class);

    @Value("${vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${vapid.subject:mailto:admin@comidapp.ar}")
    private String vapidSubject;

    private PushService pushService;

    @PostConstruct
    public void init() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        if (vapidPublicKey.isBlank() || vapidPrivateKey.isBlank()) {
            log.warn("Claves VAPID no configuradas. Web Push deshabilitado.");
            return;
        }

        try {
            pushService = new PushService()
                    .setPublicKey(vapidPublicKey)
                    .setPrivateKey(vapidPrivateKey)
                    .setSubject(vapidSubject);
            log.info("WebPushService inicializado con VAPID");
        } catch (GeneralSecurityException e) {
            log.error("Error inicializando WebPushService: {}", e.getMessage());
        }
    }

    /**
     * Envía una notificación push a una suscripción específica.
     * @return true si el envío fue exitoso
     */
    public boolean enviar(SuscripcionPush suscripcion, String payload) {
        if (pushService == null) {
            log.warn("WebPushService no inicializado, saltando envío");
            return false;
        }

        try {
            Notification notification = new Notification(
                    suscripcion.getEndpoint(),
                    suscripcion.getP256dh(),
                    suscripcion.getAuth(),
                    payload
            );

            pushService.send(notification);
            log.debug("Push enviado a endpoint: {}...",
                    suscripcion.getEndpoint().substring(0, Math.min(50, suscripcion.getEndpoint().length())));
            return true;

        } catch (Exception e) {
            log.error("Error enviando push: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Devuelve la clave pública VAPID para que el frontend se suscriba.
     */
    public String getVapidPublicKey() {
        return vapidPublicKey;
    }
}
