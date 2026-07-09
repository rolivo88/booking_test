package com.company.booking.domain.port.in;

import com.company.booking.domain.model.BookingRequest;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada: caso de uso para consultar BookingRequest.
 */
public interface FindBookingUseCase {

    Optional<BookingRequest> findById(Long id);

    Optional<BookingRequest> findByBookingCode(String bookingCode);

    List<BookingRequest> findAll();
}
