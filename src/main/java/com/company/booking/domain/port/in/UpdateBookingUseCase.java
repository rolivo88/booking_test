package com.company.booking.domain.port.in;

import com.company.booking.domain.model.BookingRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Puerto de entrada: actualización parcial de una BookingRequest.
 * Solo se permite cuando el estado es DRAFT.
 * Campos modificables: issueDate, expirationDate, fobValue, currency.
 */
public interface UpdateBookingUseCase {

    BookingRequest update(
            Long bookingId,
            LocalDate issueDate,
            LocalDate expirationDate,
            BigDecimal fobValue,
            String currency
    );
}
