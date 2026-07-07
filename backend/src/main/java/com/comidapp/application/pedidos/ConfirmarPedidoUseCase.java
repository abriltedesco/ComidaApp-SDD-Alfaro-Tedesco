package com.comidapp.application.pedidos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comidapp.domain.entities.Carrito;
import com.comidapp.domain.entities.ItemCarrito;
import com.comidapp.domain.entities.ItemPedido;
import com.comidapp.domain.entities.Pedido;
import com.comidapp.domain.entities.Repartidor;
import com.comidapp.domain.entities.Usuario;
import com.comidapp.domain.enums.EstadoPago;
import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.domain.enums.MetodoPago;
import com.comidapp.domain.enums.RolUsuario;
import com.comidapp.domain.exceptions.DomainException;
import com.comidapp.domain.exceptions.FueraDeHorarioException;
import com.comidapp.domain.exceptions.RecursoNoEncontradoException;
import com.comidapp.infrastructure.persistence.CarritoRepository;
import com.comidapp.infrastructure.persistence.PedidoRepository;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

/**
 * Caso de uso: confirmar pedido desde carrito.
 * Validación completa: carrito no vacío, local abierto, disponibilidad, asignación repartidor.
 * Migra lógica de TP3 PedidoService.crearPedido() (FR-011, FR-012, FR-013, FR-033).
 */
@Service
@Transactional
public class ConfirmarPedidoUseCase {

    private final Random random = new Random();

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Pedido confirmar(int clienteDni, MetodoPago metodoPago, String dirEntrega) {
        return confirmar(clienteDni, metodoPago, dirEntrega, null);
    }

    /**
     * Confirma un pedido a partir del carrito del cliente.
     * - Valida carrito no vacío (FR-011)
     * - Valida local abierto (FR-033)
     * - Calcula precio total (FR-012)
     * - Aplica cupón BIENVENIDOS20 si es primera compra
     * - Asigna repartidor aleatorio (migrado de TP3)
     * - Crea pedido con items y vacía carrito
     * - Pago ficticio: estadoPago = APROBADO directamente
     */
    public Pedido confirmar(int clienteDni, MetodoPago metodoPago, String dirEntrega, String cupon) {
        // Obtener carrito
        Carrito carrito = carritoRepository.findByClienteDni(clienteDni)
                .orElseThrow(() -> new RecursoNoEncontradoException("carrito", clienteDni));

        if (carrito.estaVacio()) {
            throw new DomainException("El carrito está vacío. Agregá productos antes de confirmar.");
        }

        // Validar que el local esté abierto (FR-033)
        if (carrito.getLocal() != null && !carrito.getLocal().estaAbierto()) {
            throw new FueraDeHorarioException();
        }

        // Validar disponibilidad de cada producto
        for (ItemCarrito item : carrito.getItems()) {
            if (!item.getProducto().estaDisponible()) {
                throw new DomainException(
                    "El producto '" + item.getProducto().getNombre() + "' ya no está disponible");
            }
        }

        // Asignar repartidor disponible aleatorio (FR-013 mejorado)
        List<Usuario> repartidores = usuarioRepository.findByTipo(RolUsuario.REPARTIDOR);
        List<Repartidor> disponibles = repartidores.stream()
                .filter(u -> u instanceof Repartidor r && r.isDisponible())
                .map(u -> (Repartidor) u)
                .toList();
        Repartidor repartidor = null;
        if (!disponibles.isEmpty()) {
            repartidor = disponibles.get(random.nextInt(disponibles.size()));
        }

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(carrito.getCliente());
        pedido.setLocal(carrito.getLocal());
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setEstadoPago(EstadoPago.APROBADO);
        pedido.setRepartidor(repartidor);
        pedido.setDirEntrega(dirEntrega != null && !dirEntrega.isBlank() ? dirEntrega : carrito.getCliente().getDirEntrega());
        pedido.setFechaHora(LocalDateTime.now());

        // Transferir items del carrito al pedido con precio snapshot
        for (ItemCarrito itemCarrito : carrito.getItems()) {
            ItemPedido itemPedido = new ItemPedido(
                pedido,
                itemCarrito.getProducto(),
                itemCarrito.getCantidad()
            );
            pedido.getItems().add(itemPedido);
        }

        // Calcular total
        pedido.calcularTotal();

        // Aplicar cupón BIENVENIDOS20 si es primera compra del cliente
        if (cupon != null && cupon.equalsIgnoreCase("BIENVENIDOS20")) {
            long pedidosPrevios = pedidoRepository.countPedidosValidosByClienteDni(clienteDni);
            if (pedidosPrevios > 0) {
                throw new IllegalArgumentException("El cupón BIENVENIDOS20 solo es válido para tu primer pedido");
            }
            double totalConDescuento = pedido.getPrecioTotal() * 0.8;
            pedido.setPrecioTotal(totalConDescuento);
        }

        // Guardar pedido
        Pedido guardado = pedidoRepository.save(pedido);

        // Vaciar carrito
        carrito.vaciar();
        carritoRepository.save(carrito);

        return guardado;
    }
}
