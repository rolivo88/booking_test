package com.company.booking.application.service;

import com.company.booking.domain.exception.BookingNotFoundException;
import com.company.booking.domain.exception.InvalidBookingStateException;
import com.company.booking.domain.exception.SupplierNotFoundException;
import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.Supplier;
import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.port.in.ChangeBookingStatusUseCase;
import com.company.booking.domain.port.in.CreateBookingUseCase;
import com.company.booking.domain.port.in.DeleteBookingUseCase;
import com.company.booking.domain.port.in.FindBookingUseCase;
import com.company.booking.domain.port.in.FindBookingWithFiltersUseCase;
import com.company.booking.domain.port.in.UpdateBookingUseCase;
import com.company.booking.domain.port.out.BookingRequestPort;
import com.company.booking.domain.port.out.SupplierPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación: orquesta la lógica de negocio
 * usando únicamente puertos de dominio.
 * No importa nada de infrastructure.
 */
@Service
@Transactional
public class BookingService implements
        CreateBookingUseCase,
        FindBookingUseCase,
        FindBookingWithFiltersUseCase,
        UpdateBookingUseCase,
        ChangeBookingStatusUseCase,
        DeleteBookingUseCase {

    private final BookingRequestPort bookingRequestPort;
    private final SupplierPort supplierPort;

    public BookingService(
            BookingRequestPort bookingRequestPort,
            SupplierPort supplierPort
    ) {
        this.bookingRequestPort = bookingRequestPort;
        this.supplierPort = supplierPort;
    }

    // -------------------------------------------------------------------------
    // CreateBookingUseCase
    // -------------------------------------------------------------------------

    /**
     * Reglas:
     * - El supplier debe existir por taxId.
     * - El booking debe tener al menos un item.
     * - issueDate <= expirationDate.
     * - La fecha de expiración no puede estar en el pasado.
     * - El estado inicial siempre es DRAFT.
     * - totalAmount de cada ítem es calculado en el mapper (quantity × unitPrice).
     */
    @Override
    public BookingRequest create(BookingRequest booking, String supplierTaxId) {

        Supplier supplier = supplierPort.findByTaxId(supplierTaxId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierTaxId));

        if (!booking.hasItems()) {
            throw new InvalidBookingStateException(
                    "El booking debe contener al menos un ítem"
            );
        }

        if (booking.isExpired()) {
            throw new InvalidBookingStateException(
                    "La fecha de vencimiento del booking no puede estar en el pasado"
            );
        }

        if (booking.getIssueDate() != null
                && booking.getIssueDate().isAfter(booking.getExpirationDate())) {
            throw new InvalidBookingStateException(
                    "La fecha de emisión debe ser menor o igual a la fecha de vencimiento"
            );
        }

        BookingRequest bookingWithSupplier = BookingRequest.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .issueDate(booking.getIssueDate())
                .expirationDate(booking.getExpirationDate())
                .currency(booking.getCurrency())
                .incotermCode(booking.getIncotermCode())
                .freightMode(booking.getFreightMode())
                .originCountry(booking.getOriginCountry())
                .destinationCountry(booking.getDestinationCountry())
                .fobValue(booking.getFobValue())
                .status(BookingStatus.DRAFT)
                .createdAt(booking.getCreatedAt())
                .active(true)
                .supplier(supplier)
                .items(booking.getItems())
                .build();

        return bookingRequestPort.save(bookingWithSupplier);
    }

    // -------------------------------------------------------------------------
    // FindBookingUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<BookingRequest> findById(Long id) {
        return bookingRequestPort.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookingRequest> findByBookingCode(String bookingCode) {
        return bookingRequestPort.findByBookingCode(bookingCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingRequest> findAll() {
        return bookingRequestPort.findAll();
    }

    // -------------------------------------------------------------------------
    // FindBookingWithFiltersUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<BookingRequest> findWithFilters(
            String taxId,
            BookingStatus status,
            FreightMode freightMode,
            LocalDate dateFrom,
            LocalDate dateTo,
            String bookingCode
    ) {
        return bookingRequestPort.findWithFilters(
                taxId, status, freightMode, dateFrom, dateTo, bookingCode
        );
    }

    // -------------------------------------------------------------------------
    // UpdateBookingUseCase
    // -------------------------------------------------------------------------

    /**
     * Solo se permiten actualizar: issueDate, expirationDate, fobValue, currency.
     * El booking debe estar en estado DRAFT.
     * La validación issueDate <= expirationDate vive en el dominio (withUpdatedFields).
     */
    @Override
    public BookingRequest update(
            Long bookingId,
            LocalDate issueDate,
            LocalDate expirationDate,
            BigDecimal fobValue,
            String currency
    ) {
        BookingRequest existing = bookingRequestPort.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        BookingRequest updated = existing.withUpdatedFields(
                issueDate, expirationDate, fobValue, currency
        );

        return bookingRequestPort.save(updated);
    }

    // -------------------------------------------------------------------------
    // ChangeBookingStatusUseCase
    // -------------------------------------------------------------------------

    /**
     * Transiciones permitidas:
     * DRAFT → CONFIRMED
     * DRAFT → CANCELLED
     * CONFIRMED → CANCELLED
     * CANCELLED es estado terminal — no admite más transiciones.
     *
     * La lógica de validación vive en el modelo de dominio (confirm/cancel).
     */
    @Override
    public BookingRequest changeStatus(Long bookingId, BookingStatus newStatus) {

        BookingRequest booking = bookingRequestPort.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        switch (newStatus) {
            case CONFIRMED -> booking.confirm();
            case CANCELLED -> booking.cancel();
            default -> throw new InvalidBookingStateException(
                    "No es posible transicionar al estado: " + newStatus
            );
        }

        return bookingRequestPort.save(booking);
    }

    // -------------------------------------------------------------------------
    // DeleteBookingUseCase
    // -------------------------------------------------------------------------

    /**
     * Soft delete: solo se permite para bookings en estado DRAFT o CANCELLED.
     * Marca el booking como inactivo (active = false).
     */
    @Override
    public void delete(Long bookingId) {

        BookingRequest booking = bookingRequestPort.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.DRAFT
                && booking.getStatus() != BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException(
                    "Solo se pueden eliminar bookings en estado DRAFT o CANCELADO"
            );
        }

        BookingRequest inactive = BookingRequest.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .issueDate(booking.getIssueDate())
                .expirationDate(booking.getExpirationDate())
                .currency(booking.getCurrency())
                .incotermCode(booking.getIncotermCode())
                .freightMode(booking.getFreightMode())
                .originCountry(booking.getOriginCountry())
                .destinationCountry(booking.getDestinationCountry())
                .fobValue(booking.getFobValue())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .active(false)
                .supplier(booking.getSupplier())
                .items(booking.getItems())
                .build();

        bookingRequestPort.save(inactive);
    }
}
