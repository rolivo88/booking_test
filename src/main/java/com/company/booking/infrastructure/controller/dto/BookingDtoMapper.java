package com.company.booking.infrastructure.controller.dto;

import com.company.booking.domain.model.BookingItem;
import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper estático entre DTOs de la capa HTTP y modelos de dominio.
 * Vive en infrastructure — ni el dominio ni application lo conocen.
 */
public class BookingDtoMapper {

    private BookingDtoMapper() {}

    // -------------------------------------------------------------------------
    // Request → Domain
    // -------------------------------------------------------------------------

    public static BookingRequest toDomain(CreateBookingRequest dto) {
        return BookingRequest.builder()
                .bookingCode(dto.getBookingCode())
                .issueDate(dto.getIssueDate())
                .expirationDate(dto.getExpirationDate())
                .currency(dto.getCurrency())
                .incotermCode(dto.getIncotermCode())
                .freightMode(dto.getFreightMode())
                .originCountry(dto.getOriginCountry())
                .destinationCountry(dto.getDestinationCountry())
                .fobValue(dto.getFobValue())
                .status(BookingStatus.DRAFT)
                .active(true)
                .items(toItemsDomain(dto.getItems()))
                .build();
    }

    /**
     * Regla de negocio #1: totalAmount = quantity × unitPrice.
     * Se calcula aquí en el backend; el cliente no debe enviarlo.
     */
    public static BookingItem toDomain(BookingItemRequest dto) {
        BigDecimal calculatedTotal = dto.getUnitPrice()
                .multiply(BigDecimal.valueOf(dto.getQuantity()));

        return BookingItem.builder()
                .sku(dto.getSku())
                .description(dto.getDescription())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .totalAmount(calculatedTotal)
                .build();
    }

    public static List<BookingItem> toItemsDomain(List<BookingItemRequest> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(BookingDtoMapper::toDomain)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Domain → Response
    // -------------------------------------------------------------------------

    public static BookingResponse toResponse(BookingRequest domain) {
        return BookingResponse.builder()
                .id(domain.getId())
                .bookingCode(domain.getBookingCode())
                .issueDate(domain.getIssueDate())
                .expirationDate(domain.getExpirationDate())
                .currency(domain.getCurrency())
                .incotermCode(domain.getIncotermCode())
                .freightMode(domain.getFreightMode())
                .originCountry(domain.getOriginCountry())
                .destinationCountry(domain.getDestinationCountry())
                .fobValue(domain.getFobValue())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .active(domain.getActive())
                .supplier(domain.getSupplier() == null ? null :
                        SupplierResponse.builder()
                                .id(domain.getSupplier().getId())
                                .name(domain.getSupplier().getName())
                                .taxId(domain.getSupplier().getTaxId())
                                .country(domain.getSupplier().getCountry())
                                .contactEmail(domain.getSupplier().getContactEmail())
                                .build()
                )
                .items(domain.getItems().stream()
                        .map(item -> BookingItemResponse.builder()
                                .id(item.getId())
                                .sku(item.getSku())
                                .description(item.getDescription())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalAmount(item.getTotalAmount())
                                .build()
                        )
                        .toList()
                )
                .build();
    }
}
