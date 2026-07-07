package com.comidapp.application.pagos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comidapp.domain.entities.Pago;
import com.comidapp.domain.entities.Pedido;
import com.comidapp.domain.enums.EstadoPago;
import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.domain.enums.MetodoPago;
import com.comidapp.domain.exceptions.DomainException;
import com.comidapp.domain.exceptions.RecursoNoEncontradoException;
import com.comidapp.infrastructure.persistence.PagoRepository;
import com.comidapp.infrastructure.persistence.PedidoRepository;

/**
 * Caso de uso: procesar pagos.
 * Inicio de pago (crea registro), procesamiento de webhook MP,
 * confirmación de efectivo por repartidor (FR-014, FR-017b).
 */
@Service
@Transactional
public class ProcesarPagoUseCase {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * Crea un registro de pago asociado al pedido.
     */
    public Pago iniciarPago(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("pedido", pedidoId));

        // Verificar que no exista un pago previo
        if (pagoRepository.findByPedidoId(pedidoId).isPresent()) {
            throw new DomainException("El pedido ya tiene un pago asociado");
        }

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(pedido.getMetodoPago());
        pago.setMonto(pedido.getPrecioTotal());
        pago.setEstado(EstadoPago.PENDIENTE);

        return pagoRepository.save(pago);
    }

    /**
     * Procesa la notificación de pago desde Mercado Pago (webhook).
     * Actualiza estado del pago y del pedido según resultado.
     */
    public Pago procesarWebhookMP(String mpPaymentId, EstadoPago estadoPago) {
        Pago pago = pagoRepository.findByMpPaymentId(mpPaymentId)
                .orElseThrow(() -> new RecursoNoEncontradoException("pago con MP ID", mpPaymentId));

        pago.setEstado(estadoPago);
        Pedido pedido = pago.getPedido();

        if (estadoPago == EstadoPago.APROBADO) {
            pedido.setEstadoPago(EstadoPago.APROBADO);
            if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
                pedido.avanzarA(EstadoPedido.CONFIRMADO);
            }
        } else if (estadoPago == EstadoPago.RECHAZADO) {
            pedido.setEstadoPago(EstadoPago.RECHAZADO);
        }

        pedidoRepository.save(pedido);
        return pagoRepository.save(pago);
    }

    /**
     * Confirma pago en efectivo por parte del repartidor (FR-015, FR-016).
     */
    public Pago confirmarPagoEfectivo(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("pedido", pedidoId));

        if (pedido.getMetodoPago() != MetodoPago.EFECTIVO) {
            throw new DomainException("Este pedido no es con pago en efectivo");
        }

        Pago pago = pagoRepository.findByPedidoId(pedidoId)
                .orElseGet(() -> {
                    Pago nuevo = new Pago();
                    nuevo.setPedido(pedido);
                    nuevo.setMetodoPago(MetodoPago.EFECTIVO);
                    nuevo.setMonto(pedido.getPrecioTotal());
                    return nuevo;
                });

        pago.setEstado(EstadoPago.APROBADO);
        pedido.setEstadoPago(EstadoPago.APROBADO);

        pedidoRepository.save(pedido);
        return pagoRepository.save(pago);
    }
}
