package com.comidapp.domain.exceptions;

/**
 * Recurso no encontrado (usuario, producto, pedido, etc.).
 */
public class RecursoNoEncontradoException extends DomainException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super("No se encontró " + recurso + " con identificador " + id);
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
