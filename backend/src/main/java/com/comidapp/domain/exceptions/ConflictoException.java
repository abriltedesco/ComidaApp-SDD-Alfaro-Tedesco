package com.comidapp.domain.exceptions;

/**
 * Conflicto de datos — ej: email/DNI ya en uso.
 * Migrada de TP3 ConflictoExc.
 */
public class ConflictoException extends DomainException {

    public ConflictoException() {
        super("Ocurrió un conflicto al procesar la solicitud");
    }

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
