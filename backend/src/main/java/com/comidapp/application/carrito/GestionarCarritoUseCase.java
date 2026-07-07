package com.comidapp.application.carrito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comidapp.domain.entities.Carrito;
import com.comidapp.domain.entities.Cliente;
import com.comidapp.domain.entities.Producto;
import com.comidapp.domain.entities.Usuario;
import com.comidapp.domain.exceptions.DomainException;
import com.comidapp.domain.exceptions.RecursoNoEncontradoException;
import com.comidapp.infrastructure.persistence.CarritoRepository;
import com.comidapp.infrastructure.persistence.ProductoRepository;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

/**
 * Caso de uso: gestión del carrito de compras.
 * Orquesta agregarItem, modificarCantidad, eliminarItem, vaciar (FR-007 a FR-010).
 */
@Service
@Transactional
public class GestionarCarritoUseCase {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene o crea el carrito del cliente.
     */
    public Carrito obtenerCarrito(int clienteDni) {
        return carritoRepository.findByClienteDni(clienteDni)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(clienteDni)
                            .orElseThrow(() -> new RecursoNoEncontradoException("cliente", clienteDni));
                    if (!(usuario instanceof Cliente cliente)) {
                        throw new DomainException("El usuario no es un cliente");
                    }
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setCliente(cliente);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    /**
     * Agrega un producto al carrito (FR-007, FR-007b).
     * Valida disponibilidad y restricción de un solo local.
     */
    public Carrito agregarItem(int clienteDni, Long productoId, int cantidad) {
        Carrito carrito = obtenerCarrito(clienteDni);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("producto", productoId));

        if (!producto.estaDisponible()) {
            throw new DomainException("El producto '" + producto.getNombre() + "' no está disponible");
        }

        carrito.agregarItem(producto, cantidad);
        return carritoRepository.save(carrito);
    }

    /**
     * Modifica la cantidad de un item (FR-010).
     */
    public Carrito modificarCantidad(int clienteDni, Long productoId, int nuevaCantidad) {
        Carrito carrito = obtenerCarrito(clienteDni);
        carrito.modificarCantidad(productoId, nuevaCantidad);
        return carritoRepository.save(carrito);
    }

    /**
     * Elimina un item del carrito (FR-009).
     */
    public Carrito eliminarItem(int clienteDni, Long productoId) {
        Carrito carrito = obtenerCarrito(clienteDni);
        carrito.eliminarItem(productoId);
        return carritoRepository.save(carrito);
    }

    /**
     * Vacía el carrito completo (FR-007c).
     */
    public Carrito vaciarCarrito(int clienteDni) {
        Carrito carrito = obtenerCarrito(clienteDni);
        carrito.vaciar();
        return carritoRepository.save(carrito);
    }
}
