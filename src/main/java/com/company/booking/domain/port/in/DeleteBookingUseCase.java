package com.company.booking.domain.port.in;

/**
 * Puerto de entrada: caso de uso para eliminar (soft delete) una BookingRequest.
 * Solo se permite para bookings en estado DRAFT o CANCELLED.
 */
public interface DeleteBookingUseCase {

    void delete(Long bookingId);
}
