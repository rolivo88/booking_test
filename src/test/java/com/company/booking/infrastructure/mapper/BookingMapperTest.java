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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookingMapper — conversiones dominio ↔ entidad")
class BookingMapperTest {

    private SupplierEntity supplierEntity;
    private Supplier supplier;
    private BookingItemEntity itemEntity;
    private BookingItem item;

    @BeforeEach
    void setUp() {
        supplierEntity = SupplierEntity.builder()
                .id(1L).name("Proveedor SA").taxId("12345678-9")
                .country("Chile").address("Calle 1").contactEmail("p@test.com")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();

        supplier = Supplier.builder()
                .id(1L).name("Proveedor SA").taxId("12345678-9")
                .country("Chile").address("Calle 1").contactEmail("p@test.com")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();

        itemEntity = BookingItemEntity.builder()
                .id(10L).sku("SKU-001").description("Producto")
                .quantity(5).unitPrice(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("500.00"))
                .build();

        item = BookingItem.builder()
                .id(10L).sku("SKU-001").description("Producto")
                .quantity(5).unitPrice(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("500.00"))
                .build();
    }

    // =========================================================================
    // Supplier ↔ SupplierEntity
    // =========================================================================

    @Nested
    @DisplayName("Supplier ↔ SupplierEntity")
    class SupplierMappingTests {

        @Test
        @DisplayName("toDomain(SupplierEntity) mapea todos los campos correctamente")
        void toDomain_supplier() {
            Supplier result = BookingMapper.toDomain(supplierEntity);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Proveedor SA");
            assertThat(result.getTaxId()).isEqualTo("12345678-9");
            assertThat(result.getCountry()).isEqualTo("Chile");
            assertThat(result.getAddress()).isEqualTo("Calle 1");
            assertThat(result.getContactEmail()).isEqualTo("p@test.com");
        }

        @Test
        @DisplayName("toDomain(null) retorna null")
        void toDomain_supplier_null() {
            assertThat(BookingMapper.toDomain((SupplierEntity) null)).isNull();
        }

        @Test
        @DisplayName("toEntity(Supplier) mapea todos los campos correctamente")
        void toEntity_supplier() {
            SupplierEntity result = BookingMapper.toEntity(supplier);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Proveedor SA");
            assertThat(result.getTaxId()).isEqualTo("12345678-9");
            assertThat(result.getCountry()).isEqualTo("Chile");
            assertThat(result.getAddress()).isEqualTo("Calle 1");
            assertThat(result.getContactEmail()).isEqualTo("p@test.com");
        }

        @Test
        @DisplayName("toEntity(null) retorna null")
        void toEntity_supplier_null() {
            assertThat(BookingMapper.toEntity((Supplier) null)).isNull();
        }
    }

    // =========================================================================
    // BookingItem ↔ BookingItemEntity
    // =========================================================================

    @Nested
    @DisplayName("BookingItem ↔ BookingItemEntity")
    class BookingItemMappingTests {

        @Test
        @DisplayName("toDomain(BookingItemEntity) mapea todos los campos correctamente")
        void toDomain_item() {
            BookingItem result = BookingMapper.toDomain(itemEntity);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getSku()).isEqualTo("SKU-001");
            assertThat(result.getDescription()).isEqualTo("Producto");
            assertThat(result.getQuantity()).isEqualTo(5);
            assertThat(result.getUnitPrice()).isEqualByComparingTo("100.00");
            assertThat(result.getTotalAmount()).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("toEntity(BookingItem) mapea todos los campos y asigna el booking padre")
        void toEntity_item() {
            BookingRequestEntity parentEntity = BookingRequestEntity.builder()
                    .id(99L).bookingCode("BK-PARENT").build();

            BookingItemEntity result = BookingMapper.toEntity(item, parentEntity);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getSku()).isEqualTo("SKU-001");
            assertThat(result.getQuantity()).isEqualTo(5);
            assertThat(result.getTotalAmount()).isEqualByComparingTo("500.00");
            assertThat(result.getBookingRequest()).isEqualTo(parentEntity);
        }

        @Test
        @DisplayName("toDomainItems con lista null retorna lista vacía")
        void toDomainItems_null() {
            assertThat(BookingMapper.toDomainItems(null)).isEmpty();
        }

        @Test
        @DisplayName("toDomainItems con lista vacía retorna lista vacía")
        void toDomainItems_empty() {
            assertThat(BookingMapper.toDomainItems(List.of())).isEmpty();
        }

        @Test
        @DisplayName("toDomainItems convierte todos los elementos de la lista")
        void toDomainItems_lista() {
            List<BookingItem> result = BookingMapper.toDomainItems(List.of(itemEntity, itemEntity));
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("toEntityItems con lista null retorna lista vacía")
        void toEntityItems_null() {
            assertThat(BookingMapper.toEntityItems(null, null)).isEmpty();
        }

        @Test
        @DisplayName("toEntityItems convierte todos los elementos de la lista")
        void toEntityItems_lista() {
            BookingRequestEntity parent = BookingRequestEntity.builder().id(1L).build();
            List<BookingItemEntity> result = BookingMapper.toEntityItems(List.of(item, item), parent);
            assertThat(result).hasSize(2);
        }
    }

    // =========================================================================
    // BookingRequest ↔ BookingRequestEntity
    // =========================================================================

    @Nested
    @DisplayName("BookingRequest ↔ BookingRequestEntity")
    class BookingRequestMappingTests {

        private BookingRequestEntity buildEntity() {
            BookingRequestEntity e = BookingRequestEntity.builder()
                    .id(1L).bookingCode("BK-001")
                    .issueDate(LocalDate.of(2026, 7, 1))
                    .expirationDate(LocalDate.of(2026, 12, 31))
                    .currency("USD")
                    .incotermCode("FOB").freightMode("SEA")
                    .originCountry("China").destinationCountry("Chile")
                    .fobValue(new BigDecimal("5000.00"))
                    .status(com.company.booking.infrastructure.entity.BookingStatus.DRAFT)
                    .createdAt(LocalDateTime.of(2026, 7, 1, 10, 0))
                    .active(true).supplier(supplierEntity)
                    .items(List.of(itemEntity))
                    .build();
            itemEntity.setBookingRequest(e);
            return e;
        }

        private BookingRequest buildDomain() {
            return BookingRequest.builder()
                    .id(1L).bookingCode("BK-001")
                    .issueDate(LocalDate.of(2026, 7, 1))
                    .expirationDate(LocalDate.of(2026, 12, 31))
                    .currency("USD")
                    .incotermCode(IncotermCode.FOB).freightMode(FreightMode.SEA)
                    .originCountry("China").destinationCountry("Chile")
                    .fobValue(new BigDecimal("5000.00"))
                    .status(BookingStatus.DRAFT)
                    .createdAt(LocalDateTime.of(2026, 7, 1, 10, 0))
                    .active(true).supplier(supplier)
                    .items(List.of(item))
                    .build();
        }

        @Test
        @DisplayName("toDomain(BookingRequestEntity) mapea todos los campos correctamente")
        void toDomain_booking() {
            BookingRequest result = BookingMapper.toDomain(buildEntity());

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getBookingCode()).isEqualTo("BK-001");
            assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2026, 7, 1));
            assertThat(result.getExpirationDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(result.getCurrency()).isEqualTo("USD");
            assertThat(result.getIncotermCode()).isEqualTo(IncotermCode.FOB);
            assertThat(result.getFreightMode()).isEqualTo(FreightMode.SEA);
            assertThat(result.getOriginCountry()).isEqualTo("China");
            assertThat(result.getDestinationCountry()).isEqualTo("Chile");
            assertThat(result.getFobValue()).isEqualByComparingTo("5000.00");
            assertThat(result.getStatus()).isEqualTo(BookingStatus.DRAFT);
            assertThat(result.getActive()).isTrue();
            assertThat(result.getSupplier()).isNotNull();
            assertThat(result.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("toEntity(BookingRequest) mapea todos los campos correctamente")
        void toEntity_booking() {
            BookingRequestEntity result = BookingMapper.toEntity(buildDomain());

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getBookingCode()).isEqualTo("BK-001");
            assertThat(result.getIncotermCode()).isEqualTo("FOB");
            assertThat(result.getFreightMode()).isEqualTo("SEA");
            assertThat(result.getStatus()).isEqualTo(
                    com.company.booking.infrastructure.entity.BookingStatus.DRAFT);
            assertThat(result.getActive()).isTrue();
            assertThat(result.getSupplier()).isNotNull();
            assertThat(result.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("toDomain → toEntity → toDomain mantiene los valores (round-trip)")
        void roundTrip() {
            BookingRequestEntity entity = buildEntity();
            BookingRequest domain = BookingMapper.toDomain(entity);
            BookingRequestEntity backToEntity = BookingMapper.toEntity(domain);

            assertThat(backToEntity.getBookingCode()).isEqualTo(entity.getBookingCode());
            assertThat(backToEntity.getCurrency()).isEqualTo(entity.getCurrency());
            assertThat(backToEntity.getFobValue()).isEqualByComparingTo(entity.getFobValue());
            assertThat(backToEntity.getStatus()).isEqualTo(entity.getStatus());
        }

        @Test
        @DisplayName("toDomain con lista de items null produce lista vacía en el dominio")
        void toDomain_itemsNull() {
            BookingRequestEntity e = buildEntity();
            e.setItems(null);
            BookingRequest result = BookingMapper.toDomain(e);
            assertThat(result.getItems()).isEmpty();
        }
    }
}
