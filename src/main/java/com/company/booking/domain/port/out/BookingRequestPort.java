package com.company.booking.domain.port.out;

import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: contrato que el dominio necesita
 * para persistir y consultar BookingRequest.
 * La implementación vive en infrastructure.
 */
public interface BookingRequestPort {

    BookingRequest save(BookingRequest booking);

    Optional<BookingRequest> findById(Long id);

    Optional<BookingRequest> findByBookingCode(String bookingCode);

    List<BookingRequest> findAll();

    boolean existsByBookingCode(String bookingCode);

    List<BookingRequest> findWithFilters(
            String taxId,
            BookingStatus status,
            FreightMode freightMode,
            LocalDate dateFrom,
            LocalDate dateTo,
            String bookingCode
    );
}
