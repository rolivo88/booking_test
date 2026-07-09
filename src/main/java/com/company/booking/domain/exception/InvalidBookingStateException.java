package com.company.booking.domain.exception;

/**
 * Se lanza cuando se intenta una transición de estado inválida
 * o una operación no permitida para el estado actual del booking.
 */
public class InvalidBookingStateException extends RuntimeException {

    public InvalidBookingStateException(String message) {
        super(message);
    }
}
