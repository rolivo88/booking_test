package com.company.booking.infrastructure.mapper;

import com.company.booking.domain.model.BookingItem;
import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.Supplier;
import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.model.enums.IncotermCode;
import com.company.booking.infrastructure.entity.BookingItemEntity;
import com.company.booking.infrastructure.entity.BookingRequestEntity;
import com.company.booking.infrastructure.entity.SupplierEntity;

import java.util.List;

/**
 * Mapper estático entre modelos de dominio y entidades JPA.
 * Vive en infrastructure porque conoce ambos mundos.
 * El dominio nunca importa este mapper.
 */
public class BookingMapper {

    private BookingMapper() {}

    // -------------------------------------------------------------------------
    // BookingRequest ↔ BookingRequestEntity
    // -------------------------------------------------------------------------

    public static BookingRequest toDomain(BookingRequestEntity entity) {
        return BookingRequest.builder()
                .id(entity.getId())
                .bookingCode(entity.getBookingCode())
                .issueDate(entity.getIssueDate())
                .expirationDate(entity.getExpirationDate())
                .currency(entity.getCurrency())
                .incotermCode(IncotermCode.valueOf(entity.getIncotermCode()))
                .freightMode(FreightMode.valueOf(entity.getFreightMode()))
                .originCountry(entity.getOriginCountry())
                .destinationCountry(entity.getDestinationCountry())
                .fobValue(entity.getFobValue())
                .status(BookingStatus.valueOf(entity.getStatus().name()))
                .createdAt(entity.getCreatedAt())
                .active(entity.getActive())
                .supplier(toDomain(entity.getSupplier()))
                .items(toDomainItems(entity.getItems()))
                .build();
    }

    public static BookingRequestEntity toEntity(BookingRequest domain) {
        BookingRequestEntity entity = BookingRequestEntity.builder()
                .id(domain.getId())
                .bookingCode(domain.getBookingCode())
                .issueDate(domain.getIssueDate())
                .expirationDate(domain.getExpirationDate())
                .currency(domain.getCurrency())
                .incotermCode(domain.getIncotermCode().name())
                .freightMode(domain.getFreightMode().name())
                .originCountry(domain.getOriginCountry())
                .destinationCountry(domain.getDestinationCountry())
                .fobValue(domain.getFobValue())
                .status(com.company.booking.infrastructure.entity.BookingStatus
                        .valueOf(domain.getStatus().name()))
                .createdAt(domain.getCreatedAt())
                .active(domain.getActive())
                .supplier(toEntity(domain.getSupplier()))
                .build();

        List<BookingItemEntity> itemEntities = toEntityItems(domain.getItems(), entity);
        entity.setItems(itemEntities);

        return entity;
    }

    // -------------------------------------------------------------------------
    // Supplier ↔ SupplierEntity
    // -------------------------------------------------------------------------

    public static Supplier toDomain(SupplierEntity entity) {
        if (entity == null) return null;
        return Supplier.builder()
                .id(entity.getId())
                .name(entity.getName())
                .taxId(entity.getTaxId())
                .country(entity.getCountry())
                .address(entity.getAddress())
                .contactEmail(entity.getContactEmail())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static SupplierEntity toEntity(Supplier domain) {
        if (domain == null) return null;
        return SupplierEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .taxId(domain.getTaxId())
                .country(domain.getCountry())
                .address(domain.getAddress())
                .contactEmail(domain.getContactEmail())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    // -------------------------------------------------------------------------
    // BookingItem ↔ BookingItemEntity
    // -------------------------------------------------------------------------

    public static BookingItem toDomain(BookingItemEntity entity) {
        return BookingItem.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .description(entity.getDescription())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalAmount(entity.getTotalAmount())
                .build();
    }

    public static BookingItemEntity toEntity(BookingItem domain, BookingRequestEntity bookingEntity) {
        return BookingItemEntity.builder()
                .id(domain.getId())
                .sku(domain.getSku())
                .description(domain.getDescription())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .totalAmount(domain.getTotalAmount())
                .bookingRequest(bookingEntity)
                .build();
    }

    // -------------------------------------------------------------------------
    // Listas
    // -------------------------------------------------------------------------

    public static List<BookingItem> toDomainItems(List<BookingItemEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(BookingMapper::toDomain)
                .toList();
    }

    public static List<BookingItemEntity> toEntityItems(
            List<BookingItem> items,
            BookingRequestEntity bookingEntity
    ) {
        if (items == null) return List.of();
        return items.stream()
                .map(item -> toEntity(item, bookingEntity))
                .toList();
    }
}
