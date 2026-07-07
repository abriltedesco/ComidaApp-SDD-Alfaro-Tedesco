package com.comidapp.domain.exceptions;

/**
 * Restricción del carrito: un solo local por sesión (FR-007).
 */
public class CarritoLocalException extends DomainException {

    public CarritoLocalException() {
        super("El carrito solo puede contener productos de un mismo local");
    }
}
