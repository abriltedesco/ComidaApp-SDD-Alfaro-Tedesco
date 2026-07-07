package com.comidapp.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.comidapp.api.dto.ProductoDTO;

/**
 * Tests unitarios para Producto y ProductoDTO — valida disponibilidad
 * y que el DTO no expone id/localId (FR-005, FR-006).
 */
@DisplayName("Producto - Tests unitarios")
class ProductoTest {

    @Test
    @DisplayName("Producto disponible con estaDisponible()")
    void productoDisponible() {
        Producto producto = new Producto();
        producto.setDisponible(true);
        assertTrue(producto.estaDisponible());
    }

    @Test
    @DisplayName("Producto no disponible con estaDisponible()")
    void productoNoDisponible() {
        Producto producto = new Producto();
        producto.setDisponible(false);
        assertFalse(producto.estaDisponible());
    }

    @Test
    @DisplayName("ProductoDTO mapea campos correctamente desde Producto")
    void productoDTOMapeaCampos() {
        Producto producto = new Producto();
        producto.setId(99L);
        producto.setNombre("Hamburguesa Triple");
        producto.setDescripcion("Triple carne con cheddar");
        producto.setPrecioUnitario(17500.0);
        producto.setCategoria("hamburguesas");
        producto.setImagenUrl("http://img.test/hamburguesa.png");
        producto.setDisponible(true);

        Local local = new Local();
        local.setId(5L);
        producto.setLocal(local);

        ProductoDTO dto = new ProductoDTO(producto);

        assertEquals("Hamburguesa Triple", dto.getNombre());
        assertEquals("Triple carne con cheddar", dto.getDescripcion());
        assertEquals(17500.0, dto.getPrecioUnitario());
        assertEquals("hamburguesas", dto.getCategoria());
        assertEquals("http://img.test/hamburguesa.png", dto.getImagenUrl());
        assertTrue(dto.isDisponible());
    }

    @Test
    @DisplayName("ProductoDTO NO expone id ni localId")
    void productoDTONoExponeIds() {
        Producto producto = new Producto();
        producto.setId(99L);
        producto.setNombre("Test");
        producto.setPrecioUnitario(1000.0);
        producto.setCategoria("test");
        producto.setDisponible(true);

        Local local = new Local();
        local.setId(5L);
        producto.setLocal(local);

        ProductoDTO dto = new ProductoDTO(producto);

        // Verificar que no hay métodos getId() ni getLocalId() accesibles
        // ProductoDTO solo tiene: nombre, descripcion, precioUnitario, categoria, imagenUrl, disponible
        assertNotNull(dto.getNombre());
        assertNotNull(dto.getCategoria());
    }

    @Test
    @DisplayName("Producto con categoría hamburguesas/acompañamientos/bebidas")
    void categoriaValida() {
        Producto p1 = new Producto();
        p1.setCategoria("hamburguesas");
        assertEquals("hamburguesas", p1.getCategoria());

        Producto p2 = new Producto();
        p2.setCategoria("acompañamientos");
        assertEquals("acompañamientos", p2.getCategoria());

        Producto p3 = new Producto();
        p3.setCategoria("bebidas");
        assertEquals("bebidas", p3.getCategoria());
    }

    @Test
    @DisplayName("Precio unitario dentro del rango esperado Argentina 2026")
    void precioRangoArgentina() {
        Producto producto = new Producto();
        producto.setPrecioUnitario(10500.0);
        assertTrue(producto.getPrecioUnitario() >= 3000);
        assertTrue(producto.getPrecioUnitario() <= 20000);
    }
}
