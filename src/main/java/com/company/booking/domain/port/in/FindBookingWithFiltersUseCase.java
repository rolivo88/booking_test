package com.company.booking.domain.port.in;

import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de entrada: búsqueda con filtros opcionales.
 * Todos los parámetros son opcionales; si son null se ignoran.
 */
public interface FindBookingWithFiltersUseCase {

    List<BookingRequest> findWithFilters(
        String taxId,
        BookingStatus status,
        FreightMode freightMode,
        LocalDate dateFrom,
        LocalDate dateTo,
        String bookingCode
    );
}
