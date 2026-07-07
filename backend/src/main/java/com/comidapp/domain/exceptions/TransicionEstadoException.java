package com.comidapp.domain.exceptions;

/**
 * Transición de estado inválida en un pedido.
 */
public class TransicionEstadoException extends DomainException {

    public TransicionEstadoException(String estadoActual, String estadoDestino) {
        super("No se puede pasar de " + estadoActual + " a " + estadoDestino);
    }
}
