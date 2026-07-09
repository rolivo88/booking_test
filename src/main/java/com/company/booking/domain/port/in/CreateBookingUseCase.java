package com.company.booking.domain.port.in;

import com.company.booking.domain.model.BookingRequest;

/**
 * Puerto de entrada: caso de uso para crear una BookingRequest.
 * El controlador depende de esta interfaz, no del servicio directamente.
 */
public interface CreateBookingUseCase {

    BookingRequest create(BookingRequest booking, String supplierTaxId);
}
