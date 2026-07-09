package com.company.booking.domain.port.in;

import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.enums.BookingStatus;

/**
 * Puerto de entrada: caso de uso para cambiar el estado de una BookingRequest.
 * Contempla las transiciones: DRAFT → CONFIRMED, DRAFT/CONFIRMED → CANCELLED.
 */
public interface ChangeBookingStatusUseCase {

    BookingRequest changeStatus(Long bookingId, BookingStatus newStatus);
}
