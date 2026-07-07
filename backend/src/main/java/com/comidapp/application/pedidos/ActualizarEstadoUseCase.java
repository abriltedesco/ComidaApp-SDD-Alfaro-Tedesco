package com.comidapp.application.pedidos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comidapp.domain.entities.Pedido;
import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.domain.enums.RolUsuario;
import com.comidapp.domain.exceptions.NoAutorizadoException;
import com.comidapp.domain.exceptions.RecursoNoEncontradoException;
import com.comidapp.infrastructure.persistence.PedidoRepository;

/**
 * Caso de uso: actualizar estado de un pedido.
 * Valida transición y permisos por rol (FR-017, FR-017b, FR-029, FR-030).
 */
@Service
@Transactional
public class ActualizarEstadoUseCase {

    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * Avanza el pedido al siguiente estado.
     * @param pedidoId ID del pedido
     * @param nuevoEstado estado destino
     * @param actorDni DNI del usuario que realiza la acción
     * @param actorRol rol del actor
     */
    public Pedido actualizar(Long pedidoId, EstadoPedido nuevoEstado, int actorDni, RolUsuario actorRol) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("pedido", pedidoId));

        validarPermisos(pedido, nuevoEstado, actorDni, actorRol);

        // Delega validación de transición a la entidad
        pedido.avanzarA(nuevoEstado);

        return pedidoRepository.save(pedido);
    }

    /**
     * Cancela un pedido (FR-017b).
     * Solo el cliente dueño o un admin pueden cancelar.
     */
    public Pedido cancelar(Long pedidoId, int actorDni, RolUsuario actorRol) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("pedido", pedidoId));

        boolean esDuenio = pedido.getCliente().getDni() == actorDni;
        boolean esAdmin = actorRol == RolUsuario.ADMIN;

        if (!esDuenio && !esAdmin) {
            throw new NoAutorizadoException("Solo el cliente dueño o un admin pueden cancelar el pedido");
        }

        pedido.cancelar();
        return pedidoRepository.save(pedido);
    }

    /**
     * Valida permisos según el rol y el estado destino.
     * - Admin: puede hacer cualquier transición
     * - Repartidor: EN_CAMINO → ENTREGADO (FR-030)
     * - Cliente: solo cancelar (manejado aparte)
     */
    private void validarPermisos(Pedido pedido, EstadoPedido nuevoEstado, int actorDni, RolUsuario actorRol) {
        if (actorRol == RolUsuario.ADMIN) {
            return; // Admin puede todo
        }

        if (actorRol == RolUsuario.REPARTIDOR) {
            // Repartidor puede avanzar sus pedidos asignados por todo el flujo
            if (pedido.getRepartidor() != null
                    && pedido.getRepartidor().getDni() == actorDni) {
                return;
            }
            throw new NoAutorizadoException("El repartidor solo puede gestionar sus pedidos asignados");
        }

        if (actorRol == RolUsuario.CLIENTE) {
            // Cliente puede confirmar recepción: ENTREGADO → FINALIZADO
            if (nuevoEstado == EstadoPedido.FINALIZADO
                    && pedido.getCliente().getDni() == actorDni) {
                return;
            }
        }

        throw new NoAutorizadoException();
    }
}
