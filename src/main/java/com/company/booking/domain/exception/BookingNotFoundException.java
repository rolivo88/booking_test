package com.company.booking.domain.exception;

/**
 * Se lanza cuando no se encuentra una BookingRequest por el criterio indicado.
 */
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long id) {
        super("Booking no encontrado con id: " + id);
    }

    public BookingNotFoundException(String bookingCode) {
        super("Booking no encontrado con código: " + bookingCode);
    }
}
