package com.company.booking.infrastructure.adapter.out;

import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.port.out.BookingRequestPort;
import com.company.booking.infrastructure.mapper.BookingMapper;
import com.company.booking.infrastructure.repository.BookingRequestRepository;
import com.company.booking.infrastructure.repository.BookingSpecification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: implementa BookingRequestPort usando Spring Data JPA.
 * Es el único lugar donde el repositorio JPA y las Specifications son conocidos.
 */
@Component
public class BookingRequestAdapter implements BookingRequestPort {

    private final BookingRequestRepository repository;

    public BookingRequestAdapter(BookingRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public BookingRequest save(BookingRequest booking) {
        return BookingMapper.toDomain(
            repository.save(BookingMapper.toEntity(booking))
        );
    }

    @Override
    public Optional<BookingRequest> findById(Long id) {
        return repository.findById(id)
            .map(BookingMapper::toDomain);
    }

    @Override
    public Optional<BookingRequest> findByBookingCode(String bookingCode) {
        return repository.findByBookingCode(bookingCode)
            .map(BookingMapper::toDomain);
    }

    @Override
    public List<BookingRequest> findAll() {
        return repository.findAll()
            .stream()
            .map(BookingMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByBookingCode(String bookingCode) {
        return repository.existsByBookingCode(bookingCode);
    }

    @Override
    public List<BookingRequest> findWithFilters(
            String taxId,
            BookingStatus status,
            FreightMode freightMode,
            LocalDate dateFrom,
            LocalDate dateTo,
            String bookingCode
    ) {
        return repository.findAll(
                BookingSpecification.withFilters(
                    taxId, status, freightMode, dateFrom, dateTo, bookingCode
                )
            )
            .stream()
            .map(BookingMapper::toDomain)
            .toList();
    }
}
