package com.comidapp.domain.entities;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.domain.enums.MetodoPago;
import com.comidapp.domain.exceptions.TransicionEstadoException;

/**
 * Tests unitarios para la entidad Pedido — valida transiciones de estado,
 * cálculo de total y lógica de vencimiento (FR-017, FR-017b, FR-032).
 */
@DisplayName("Pedido - Tests unitarios")
class PedidoTest {

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setMetodoPago(MetodoPago.EFECTIVO);
        pedido.setFechaHora(LocalDateTime.now());
        pedido.setPrecioTotal(0.0);
    }

    // ── Transiciones de estado válidas ──

    @Test
    @DisplayName("PENDIENTE → CONFIRMADO es válido")
    void pendienteAConfirmado() {
        pedido.avanzarA(EstadoPedido.CONFIRMADO);
        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
    }

    @Test
    @DisplayName("CONFIRMADO → EN_PREPARACION es válido")
    void confirmadoAEnPreparacion() {
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.avanzarA(EstadoPedido.EN_PREPARACION);
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());
    }

    @Test
    @DisplayName("EN_PREPARACION → EN_CAMINO es válido")
    void enPreparacionAEnCamino() {
        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        pedido.avanzarA(EstadoPedido.EN_CAMINO);
        assertEquals(EstadoPedido.EN_CAMINO, pedido.getEstado());
    }

    @Test
    @DisplayName("EN_CAMINO → ENTREGADO es válido")
    void enCaminoAEntregado() {
        pedido.setEstado(EstadoPedido.EN_CAMINO);
        pedido.avanzarA(EstadoPedido.ENTREGADO);
        assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
    }

    // ── Transiciones inválidas ──

    @Test
    @DisplayName("PENDIENTE → ENTREGADO lanza excepción")
    void pendienteAEntregadoFalla() {
        assertThrows(TransicionEstadoException.class,
                () -> pedido.avanzarA(EstadoPedido.ENTREGADO));
    }

    @Test
    @DisplayName("ENTREGADO no puede transicionar a nada")
    void entregadoNoTransiciona() {
        pedido.setEstado(EstadoPedido.ENTREGADO);
        assertThrows(TransicionEstadoException.class,
                () -> pedido.avanzarA(EstadoPedido.PENDIENTE));
    }

    @Test
    @DisplayName("CANCELADO no puede transicionar a nada")
    void canceladoNoTransiciona() {
        pedido.setEstado(EstadoPedido.CANCELADO);
        assertThrows(TransicionEstadoException.class,
                () -> pedido.avanzarA(EstadoPedido.CONFIRMADO));
    }

    // ── Cancelación ──

    @Test
    @DisplayName("Pedido PENDIENTE puede cancelarse")
    void pendientePuedeCancelarse() {
        pedido.cancelar();
        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
    }

    @Test
    @DisplayName("Pedido EN_CAMINO no puede cancelarse")
    void enCaminoNoPuedeCancelarse() {
        pedido.setEstado(EstadoPedido.EN_CAMINO);
        assertThrows(TransicionEstadoException.class, () -> pedido.cancelar());
    }

    // ── Vencimiento ──

    @Test
    @DisplayName("Pedido PENDIENTE de hace 2 horas está vencido")
    void pedidoVencido() {
        pedido.setFechaHora(LocalDateTime.now().minusHours(2));
        assertTrue(pedido.estaVencido());
    }

    @Test
    @DisplayName("Pedido PENDIENTE reciente NO está vencido")
    void pedidoNoVencido() {
        pedido.setFechaHora(LocalDateTime.now().minusMinutes(30));
        assertFalse(pedido.estaVencido());
    }

    @Test
    @DisplayName("Pedido CONFIRMADO nunca está vencido")
    void confirmadoNoVencido() {
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setFechaHora(LocalDateTime.now().minusHours(5));
        assertFalse(pedido.estaVencido());
    }

    // ── Cálculo de total ──

    @Test
    @DisplayName("calcularTotal suma items correctamente")
    void calcularTotal() {
        Producto p1 = new Producto();
        p1.setPrecioUnitario(10500.0);
        Producto p2 = new Producto();
        p2.setPrecioUnitario(3500.0);

        ItemPedido item1 = new ItemPedido(pedido, p1, 2);
        ItemPedido item2 = new ItemPedido(pedido, p2, 3);
        pedido.getItems().add(item1);
        pedido.getItems().add(item2);

        pedido.calcularTotal();

        assertEquals(31500.0, pedido.getPrecioTotal(), 0.01);
    }
}
