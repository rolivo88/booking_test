package com.company.booking.infrastructure.adapter.out;

import com.company.booking.domain.model.BookingItem;
import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.Supplier;
import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.model.enums.IncotermCode;
import com.company.booking.infrastructure.entity.BookingItemEntity;
import com.company.booking.infrastructure.entity.BookingRequestEntity;
import com.company.booking.infrastructure.entity.SupplierEntity;
import com.company.booking.infrastructure.repository.BookingRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingRequestAdapter — pruebas unitarias")
class BookingRequestAdapterTest {

    @Mock
    private BookingRequestRepository repository;

    @InjectMocks
    private BookingRequestAdapter adapter;

    private SupplierEntity supplierEntity;
    private BookingItemEntity itemEntity;
    private BookingRequestEntity bookingEntity;

    @BeforeEach
    void setUp() {
        supplierEntity = SupplierEntity.builder()
                .id(1L).name("Proveedor SA").taxId("12345678-9")
                .country("Chile").address("Calle 1").contactEmail("p@test.com")
                .createdAt(LocalDateTime.now()).build();

        itemEntity = BookingItemEntity.builder()
                .id(10L).sku("SKU-001").description("Producto")
                .quantity(5).unitPrice(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("500.00")).build();

        bookingEntity = BookingRequestEntity.builder()
                .id(1L).bookingCode("BK-001")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(30))
                .currency("USD").incotermCode("FOB").freightMode("SEA")
                .originCountry("China").destinationCountry("Chile")
                .fobValue(new BigDecimal("5000.00"))
                .status(com.company.booking.infrastructure.entity.BookingStatus.DRAFT)
                .createdAt(LocalDateTime.now()).active(true)
                .supplier(supplierEntity).items(List.of(itemEntity)).build();

        itemEntity.setBookingRequest(bookingEntity);
    }

    private BookingRequest buildDomainBooking(BookingStatus status) {
        Supplier supplier = Supplier.builder()
                .id(1L).name("Proveedor SA").taxId("12345678-9")
                .country("Chile").address("Calle 1").contactEmail("p@test.com")
                .createdAt(LocalDateTime.now()).build();

        BookingItem item = BookingItem.builder()
                .id(10L).sku("SKU-001").description("Producto")
                .quantity(5).unitPrice(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("500.00")).build();

        return BookingRequest.builder()
                .id(1L).bookingCode("BK-001")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(30))
                .currency("USD").incotermCode(IncotermCode.FOB).freightMode(FreightMode.SEA)
                .originCountry("China").destinationCountry("Chile")
                .fobValue(new BigDecimal("5000.00"))
                .status(status).createdAt(LocalDateTime.now())
                .active(true).supplier(supplier).items(List.of(item)).build();
    }

    @Nested
    @DisplayName("save()")
    class SaveTests {

        @Test
        @DisplayName("guarda y retorna el booking como dominio")
        void save_ok() {
            when(repository.save(any())).thenReturn(bookingEntity);

            BookingRequest result = adapter.save(buildDomainBooking(BookingStatus.DRAFT));

            assertThat(result).isNotNull();
            assertThat(result.getBookingCode()).isEqualTo("BK-001");
            assertThat(result.getStatus()).isEqualTo(BookingStatus.DRAFT);
            verify(repository).save(any());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("retorna el booking cuando existe")
        void findById_encontrado() {
            when(repository.findById(1L)).thenReturn(Optional.of(bookingEntity));

            Optional<BookingRequest> result = adapter.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando no existe")
        void findById_noEncontrado() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThat(adapter.findById(99L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByBookingCode()")
    class FindByCodeTests {

        @Test
        @DisplayName("retorna el booking cuando el código existe")
        void findByBookingCode_encontrado() {
            when(repository.findByBookingCode("BK-001")).thenReturn(Optional.of(bookingEntity));

            Optional<BookingRequest> result = adapter.findByBookingCode("BK-001");

            assertThat(result).isPresent();
            assertThat(result.get().getBookingCode()).isEqualTo("BK-001");
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el código no existe")
        void findByBookingCode_noEncontrado() {
            when(repository.findByBookingCode("NOPE")).thenReturn(Optional.empty());

            assertThat(adapter.findByBookingCode("NOPE")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("retorna todos los bookings")
        void findAll_ok() {
            when(repository.findAll()).thenReturn(List.of(bookingEntity, bookingEntity));

            List<BookingRequest> result = adapter.findAll();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay registros")
        void findAll_vacio() {
            when(repository.findAll()).thenReturn(List.of());

            assertThat(adapter.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByBookingCode()")
    class ExistsTests {

        @Test
        @DisplayName("retorna true cuando el código existe")
        void exists_true() {
            when(repository.existsByBookingCode("BK-001")).thenReturn(true);
            assertThat(adapter.existsByBookingCode("BK-001")).isTrue();
        }

        @Test
        @DisplayName("retorna false cuando el código no existe")
        void exists_false() {
            when(repository.existsByBookingCode("NOPE")).thenReturn(false);
            assertThat(adapter.existsByBookingCode("NOPE")).isFalse();
        }
    }

    @Nested
    @DisplayName("findWithFilters()")
    class FindWithFiltersTests {

        @Test
        @DisplayName("delega al repositorio con Specification y convierte resultados")
        void findWithFilters_ok() {
            when(repository.findAll(any(Specification.class)))
                    .thenReturn(List.of(bookingEntity));

            List<BookingRequest> result = adapter.findWithFilters(
                    "12345678-9", BookingStatus.DRAFT, FreightMode.SEA,
                    null, null, null
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.DRAFT);
            verify(repository).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("retorna lista vacía cuando la Specification no encuentra resultados")
        void findWithFilters_sinResultados() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of());

            List<BookingRequest> result = adapter.findWithFilters(
                    null, null, null, null, null, null
            );

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("acepta rango de fechas sin lanzar excepción")
        void findWithFilters_conFechas() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(bookingEntity));

            List<BookingRequest> result = adapter.findWithFilters(
                    null, null, null,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    null
            );

            assertThat(result).hasSize(1);
        }
    }
}
