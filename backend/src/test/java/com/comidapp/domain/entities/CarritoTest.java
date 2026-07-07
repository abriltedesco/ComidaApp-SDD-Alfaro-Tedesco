package com.comidapp.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.comidapp.domain.exceptions.CarritoLocalException;

/**
 * Tests unitarios para la entidad Carrito — restricción un solo local,
 * agregar/eliminar items, calcular total (FR-007 a FR-010).
 */
@DisplayName("Carrito - Tests unitarios")
class CarritoTest {

    private Carrito carrito;
    private Local local1;
    private Local local2;
    private Producto producto1;
    private Producto producto2;
    private Producto productoOtroLocal;

    @BeforeEach
    void setUp() {
        carrito = new Carrito();

        local1 = new Local();
        local1.setId(1L);
        local1.setNombre("Local 1");

        local2 = new Local();
        local2.setId(2L);
        local2.setNombre("Local 2");

        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Hamburguesa Simple");
        producto1.setPrecioUnitario(10500.0);
        producto1.setLocal(local1);
        producto1.setDisponible(true);

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Papas Fritas");
        producto2.setPrecioUnitario(6500.0);
        producto2.setLocal(local1);
        producto2.setDisponible(true);

        productoOtroLocal = new Producto();
        productoOtroLocal.setId(3L);
        productoOtroLocal.setNombre("Hamburguesa de otro local");
        productoOtroLocal.setPrecioUnitario(11000.0);
        productoOtroLocal.setLocal(local2);
        productoOtroLocal.setDisponible(true);
    }

    @Test
    @DisplayName("Agregar item al carrito vacío funciona")
    void agregarItemCarritoVacio() {
        carrito.agregarItem(producto1, 2);

        assertEquals(1, carrito.getItems().size());
        assertEquals(local1, carrito.getLocal());
    }

    @Test
    @DisplayName("Agregar mismo producto suma cantidad")
    void agregarMismoProductoSumaCantidad() {
        carrito.agregarItem(producto1, 2);
        carrito.agregarItem(producto1, 3);

        assertEquals(1, carrito.getItems().size());
        assertEquals(5, carrito.getItems().get(0).getCantidad());
    }

    @Test
    @DisplayName("Agregar producto de otro local lanza CarritoLocalException")
    void agregarProductoOtroLocalFalla() {
        carrito.agregarItem(producto1, 1);

        assertThrows(CarritoLocalException.class,
                () -> carrito.agregarItem(productoOtroLocal, 1));
    }

    @Test
    @DisplayName("Agregar múltiples productos del mismo local funciona")
    void agregarMultiplesProductosMismoLocal() {
        carrito.agregarItem(producto1, 1);
        carrito.agregarItem(producto2, 2);

        assertEquals(2, carrito.getItems().size());
    }

    @Test
    @DisplayName("Eliminar item reduce la lista")
    void eliminarItem() {
        carrito.agregarItem(producto1, 1);
        carrito.agregarItem(producto2, 2);

        carrito.eliminarItem(producto1.getId());

        assertEquals(1, carrito.getItems().size());
        assertEquals("Papas Fritas", carrito.getItems().get(0).getProducto().getNombre());
    }

    @Test
    @DisplayName("Eliminar último item resetea el local")
    void eliminarUltimoItemReseteaLocal() {
        carrito.agregarItem(producto1, 1);
        carrito.eliminarItem(producto1.getId());

        assertNull(carrito.getLocal());
        assertTrue(carrito.estaVacio());
    }

    @Test
    @DisplayName("Modificar cantidad de un item")
    void modificarCantidad() {
        carrito.agregarItem(producto1, 2);
        carrito.modificarCantidad(producto1.getId(), 5);

        assertEquals(5, carrito.getItems().get(0).getCantidad());
    }

    @Test
    @DisplayName("Modificar cantidad a 0 elimina el item")
    void modificarCantidadACeroElimina() {
        carrito.agregarItem(producto1, 2);
        carrito.modificarCantidad(producto1.getId(), 0);

        assertTrue(carrito.estaVacio());
    }

    @Test
    @DisplayName("Vaciar carrito elimina todos los items y resetea local")
    void vaciarCarrito() {
        carrito.agregarItem(producto1, 1);
        carrito.agregarItem(producto2, 3);

        carrito.vaciar();

        assertTrue(carrito.estaVacio());
        assertNull(carrito.getLocal());
    }

    @Test
    @DisplayName("Calcular total suma correctamente")
    void calcularTotal() {
        carrito.agregarItem(producto1, 2); // 10500 * 2 = 21000
        carrito.agregarItem(producto2, 1); // 6500 * 1 = 6500

        assertEquals(27500.0, carrito.calcularTotal(), 0.01);
    }

    @Test
    @DisplayName("Carrito vacío tiene total 0")
    void carritoVacioTotalCero() {
        assertEquals(0.0, carrito.calcularTotal(), 0.01);
    }
}
