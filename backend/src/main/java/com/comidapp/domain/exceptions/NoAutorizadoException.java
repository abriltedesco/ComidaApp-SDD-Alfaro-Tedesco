package com.comidapp.domain.exceptions;

/**
 * Acción no autorizada para el rol actual.
 * Migrada de TP3 NoAutorizadoException.
 */
public class NoAutorizadoException extends DomainException {

    public NoAutorizadoException() {
        super("No tenés permiso para realizar esta acción");
    }

    public NoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
