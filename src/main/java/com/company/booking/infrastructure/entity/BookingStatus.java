package com.company.booking.infrastructure.entity;

/**
 * Enum de persistencia para el estado del booking.
 * No contiene lógica de negocio — esa vive en domain.model.enums.BookingStatus.
 * El BookingMapper se encarga de convertir entre ambos.
 */
public enum BookingStatus {
    DRAFT,
    CONFIRMED,
    CANCELLED
}
