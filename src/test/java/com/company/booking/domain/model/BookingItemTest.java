package com.company.booking.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del modelo de dominio BookingItem.
 * Valida las reglas de negocio de cantidad y monto.
 */
@DisplayName("BookingItem — reglas de dominio")
class BookingItemTest {

    @Test
    @DisplayName("hasValidQuantity() retorna true para cantidad positiva")
    void hasValidQuantity_positivo() {
        BookingItem item = BookingItem.builder()
                .sku("SKU-001")
                .description("Producto")
                .quantity(5)
                .unitPrice(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("50.00"))
                .build();

        assertThat(item.hasValidQuantity()).isTrue();
    }

    @Test
    @DisplayName("hasValidQuantity() retorna false para cantidad cero")
    void hasValidQuantity_cero() {
        BookingItem item = BookingItem.builder()
                .sku("SKU-002")
                .description("Producto")
                .quantity(0)
                .unitPrice(new BigDecimal("10.00"))
                .totalAmount(BigDecimal.ZERO)
                .build();

        assertThat(item.hasValidQuantity()).isFalse();
    }

    @Test
    @DisplayName("hasValidQuantity() retorna false para cantidad null")
    void hasValidQuantity_null() {
        BookingItem item = BookingItem.builder()
                .sku("SKU-003")
                .description("Producto")
                .quantity(null)
                .unitPrice(new BigDecimal("10.00"))
                .totalAmount(BigDecimal.ZERO)
                .build();

        assertThat(item.hasValidQuantity()).isFalse();
    }

    @Test
    @DisplayName("hasValidAmount() retorna true para totalAmount igual a cero")
    void hasValidAmount_cero() {
        BookingItem item = BookingItem.builder()
                .sku("SKU-004")
                .description("Producto")
                .quantity(1)
                .unitPrice(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        assertThat(item.hasValidAmount()).isTrue();
    }

    @Test
    @DisplayName("hasValidAmount() retorna true para totalAmount positivo")
    void hasValidAmount_positivo() {
        BookingItem item = BookingItem.builder()
                .sku("SKU-005")
                .description("Producto")
                .quantity(2)
                .unitPrice(new BigDecimal("25.00"))
                .totalAmount(new BigDecimal("50.00"))
                .build();

        assertThat(item.hasValidAmount()).isTrue();
    }

    @Test
    @DisplayName("hasValidAmount() retorna false para totalAmount null")
    void hasValidAmount_null() {
        BookingItem item = BookingItem.builder()
                .sku("SKU-006")
                .description("Producto")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .totalAmount(null)
                .build();

        assertThat(item.hasValidAmount()).isFalse();
    }
}
