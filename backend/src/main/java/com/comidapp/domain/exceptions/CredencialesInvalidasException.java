package com.comidapp.domain.exceptions;

/**
 * Credenciales de login inválidas.
 * Migrada de TP3 CredencialesInvalidasException.
 */
public class CredencialesInvalidasException extends DomainException {

    public CredencialesInvalidasException() {
        super("Ingresaste mal tus datos. Verificá tu email y contraseña.");
    }

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
