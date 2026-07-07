package com.comidapp.domain.exceptions;

/**
 * El local está fuera de horario de atención (FR-033).
 */
public class FueraDeHorarioException extends DomainException {

    public FueraDeHorarioException() {
        super("El local se encuentra fuera de horario de atención");
    }

    public FueraDeHorarioException(String mensaje) {
        super(mensaje);
    }
}
