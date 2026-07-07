package com.comidapp.domain.exceptions;

/**
 * Excepción base de dominio para reglas de negocio.
 */
public class DomainException extends RuntimeException {

    public DomainException(String mensaje) {
        super(mensaje);
    }

    public DomainException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
