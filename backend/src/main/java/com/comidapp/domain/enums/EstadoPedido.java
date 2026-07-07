package com.comidapp.domain.enums;

/**
 * Estados del ciclo de vida de un pedido.
 * Migrado de TP3 (campo String) a enum tipado.
 */
public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    EN_PREPARACION,
    EN_CAMINO,
    ENTREGADO,
    FINALIZADO,
    CANCELADO;

    /**
     * Valida si la transición de estado es permitida.
     */
    public boolean puedeTransicionarA(EstadoPedido siguiente) {
        return switch (this) {
            case PENDIENTE       -> siguiente == CONFIRMADO || siguiente == CANCELADO;
            case CONFIRMADO      -> siguiente == EN_PREPARACION || siguiente == EN_CAMINO || siguiente == CANCELADO;
            case EN_PREPARACION  -> siguiente == EN_CAMINO || siguiente == CANCELADO;
            case EN_CAMINO       -> siguiente == ENTREGADO;
            case ENTREGADO       -> siguiente == FINALIZADO;
            case FINALIZADO      -> false;
            case CANCELADO       -> false;
        };
    }
}
