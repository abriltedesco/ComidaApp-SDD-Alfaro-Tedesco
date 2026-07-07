package com.comidapp.infrastructure.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

/**
 * Adaptador de infraestructura para Mercado Pago SDK.
 * Crea preferencias de pago y consulta estado (FR-014, FR-017b).
 */
@Service
public class MercadoPagoAdapter {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoAdapter.class);

    @Value("${mp.access-token}")
    private String accessToken;

    @Value("${mp.success-url}")
    private String successUrl;

    @Value("${mp.failure-url}")
    private String failureUrl;

    @Value("${mp.pending-url}")
    private String pendingUrl;

    /**
     * Crea una preferencia de pago en Mercado Pago.
     * @return ID de la preferencia (para redirigir al cliente al checkout MP)
     */
    public String crearPreferencia(Long pedidoId, String descripcion, BigDecimal monto) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(descripcion)
                    .quantity(1)
                    .unitPrice(monto)
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(String.valueOf(pedidoId))
                    .autoReturn("approved")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);

            log.info("Preferencia MP creada para pedido #{}: {}", pedidoId, preference.getId());
            return preference.getId();

        } catch (Exception e) {
            log.error("Error creando preferencia MP para pedido #{}: {}", pedidoId, e.getMessage());
            throw new RuntimeException("Error al crear preferencia de pago en Mercado Pago", e);
        }
    }
}
