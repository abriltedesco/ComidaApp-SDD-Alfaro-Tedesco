package com.comidapp.application.pedidos;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comidapp.domain.entities.Pedido;
import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.infrastructure.persistence.PedidoRepository;

/**
 * Caso de uso: cancelar automáticamente pedidos vencidos.
 * Un pedido en estado PENDIENTE por más de 1 hora se cancela (FR-032).
 */
@Service
public class CancelarPedidoVencidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelarPedidoVencidoUseCase.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * Ejecuta cada 5 minutos, busca pedidos PENDIENTE con más de 1 hora
     * y los cancela automáticamente.
     */
    @Scheduled(fixedRate = 300_000) // cada 5 minutos
    @Transactional
    public void cancelarVencidos() {
        LocalDateTime limite = LocalDateTime.now().minusHours(1);
        List<Pedido> vencidos = pedidoRepository.findVencidos(EstadoPedido.PENDIENTE, limite);

        for (Pedido pedido : vencidos) {
            pedido.cancelar();
            pedidoRepository.save(pedido);
            log.info("Pedido #{} cancelado por timeout (vencido desde {})", pedido.getId(), pedido.getFechaHora());
        }

        if (!vencidos.isEmpty()) {
            log.info("Se cancelaron {} pedidos vencidos", vencidos.size());
        }
    }
}
