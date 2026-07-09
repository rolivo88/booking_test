package com.company.booking.application.service;

import com.company.booking.domain.exception.BookingNotFoundException;
import com.company.booking.domain.exception.InvalidBookingStateException;
import com.company.booking.domain.exception.SupplierNotFoundException;
import com.company.booking.domain.model.BookingItem;
import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.Supplier;
import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.model.enums.IncotermCode;
import com.company.booking.domain.port.out.BookingRequestPort;
import com.company.booking.domain.port.out.SupplierPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests adicionales de BookingService para aumentar cobertura.
 * Cubre: findByBookingCode, mensajes de excepciones, campos del booking
 * creado/eliminado, update con todos los campos null, findWithFilters
 * con distintas combinaciones.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService — pruebas extendidas")
class BookingServiceExtendedTest {

    @Mock
    private BookingRequestPort bookingRequestPort;

    @Mock
    private SupplierPort supplierPort;

    @InjectMocks
    private BookingService bookingService;

    private Supplier supplier;
    private BookingItem item;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .id(1L)
                .name("Proveedor SA")
                .taxId("12345678-9")
                .country("Chile")
                .contactEmail("proveedor@test.com")
                .createdAt(LocalDateTime.now())
                .build();

        item = BookingItem.builder()
                .sku("SKU-001")
                .description("Producto de prueba")
                .quantity(10)
                .unitPrice(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("500.00"))
                .build();
    }

    private BookingRequest buildSavedBooking(BookingStatus status) {
        return BookingRequest.builder()
                .id(1L)
                .bookingCode("BK-001")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(30))
                .currency("USD")
                .incotermCode(IncotermCode.FOB)
                .freightMode(FreightMode.SEA)
                .originCountry("China")
                .destinationCountry("Chile")
                .fobValue(new BigDecimal("5000.00"))
                .status(status)
                .createdAt(LocalDateTime.now())
                .active(true)
                .supplier(supplier)
                .items(List.of(item))
                .build();
    }

    // =========================================================================
    // create() — campos del booking resultante
    // =========================================================================

    @Nested
    @DisplayName("create() — campos del booking resultante")
    class CreateFieldsTests {

        @Test
        @DisplayName("el supplier asignado es el encontrado por taxId")
        void create_supplierAsignadoCorrectamente() {
            BookingRequest input = BookingRequest.builder()
                    .bookingCode("BK-X")
                    .issueDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusDays(10))
                    .currency("USD")
                    .incotermCode(IncotermCode.FOB)
                    .freightMode(FreightMode.SEA)
                    .originCountry("China")
                    .destinationCountry("Chile")
                    .fobValue(new BigDecimal("1000.00"))
                    .status(BookingStatus.DRAFT)
                    .active(true)
                    .items(List.of(item))
                    .build();

            when(supplierPort.findByTaxId("12345678-9")).thenReturn(Optional.of(supplier));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BookingRequest result = bookingService.create(input, "12345678-9");

            assertThat(result.getSupplier()).isNotNull();
            assertThat(result.getSupplier().getId()).isEqualTo(1L);
            assertThat(result.getSupplier().getName()).isEqualTo("Proveedor SA");
        }

        @Test
        @DisplayName("active siempre es true en el booking recién creado")
        void create_activoEsTrue() {
            BookingRequest input = BookingRequest.builder()
                    .bookingCode("BK-Y")
                    .issueDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusDays(10))
                    .currency("USD")
                    .incotermCode(IncotermCode.CIF)
                    .freightMode(FreightMode.AIR)
                    .originCountry("China")
                    .destinationCountry("Chile")
                    .fobValue(new BigDecimal("2000.00"))
                    .status(BookingStatus.DRAFT)
                    .active(true)
                    .items(List.of(item))
                    .build();

            when(supplierPort.findByTaxId("12345678-9")).thenReturn(Optional.of(supplier));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BookingRequest result = bookingService.create(input, "12345678-9");

            assertThat(result.getActive()).isTrue();
        }

        @Test
        @DisplayName("mensaje de SupplierNotFoundException contiene el taxId buscado")
        void create_mensajeExcepcionContieneTaxId() {
            BookingRequest input = BookingRequest.builder()
                    .bookingCode("BK-Z")
                    .issueDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusDays(10))
                    .currency("USD")
                    .incotermCode(IncotermCode.FOB)
                    .freightMode(FreightMode.SEA)
                    .originCountry("China")
                    .destinationCountry("Chile")
                    .fobValue(new BigDecimal("500.00"))
                    .status(BookingStatus.DRAFT)
                    .active(true)
                    .items(List.of(item))
                    .build();

            when(supplierPort.findByTaxId("TAX-NOEXISTE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.create(input, "TAX-NOEXISTE"))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessageContaining("TAX-NOEXISTE");
        }
    }

    // =========================================================================
    // findByBookingCode()
    // =========================================================================

    @Nested
    @DisplayName("findByBookingCode()")
    class FindByCodeTests {

        @Test
        @DisplayName("retorna el booking cuando el código existe")
        void findByBookingCode_encontrado() {
            BookingRequest saved = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findByBookingCode("BK-001")).thenReturn(Optional.of(saved));

            Optional<BookingRequest> result = bookingService.findByBookingCode("BK-001");

            assertThat(result).isPresent();
            assertThat(result.get().getBookingCode()).isEqualTo("BK-001");
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el código no existe")
        void findByBookingCode_noEncontrado() {
            when(bookingRequestPort.findByBookingCode("INEXISTENTE")).thenReturn(Optional.empty());

            Optional<BookingRequest> result = bookingService.findByBookingCode("INEXISTENTE");

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // update() — casos adicionales
    // =========================================================================

    @Nested
    @DisplayName("update() — casos adicionales")
    class UpdateExtendedTests {

        @Test
        @DisplayName("cuando todos los campos son null conserva los valores originales")
        void update_todosNullConservaValores() {
            BookingRequest draft = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(draft));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BookingRequest result = bookingService.update(1L, null, null, null, null);

            assertThat(result.getIssueDate()).isEqualTo(draft.getIssueDate());
            assertThat(result.getExpirationDate()).isEqualTo(draft.getExpirationDate());
            assertThat(result.getFobValue()).isEqualByComparingTo(draft.getFobValue());
            assertThat(result.getCurrency()).isEqualTo(draft.getCurrency());
        }

        @Test
        @DisplayName("lanza IllegalStateException al intentar actualizar un booking CANCELLED")
        void update_noPermitidoEnCancelled() {
            BookingRequest cancelled = buildSavedBooking(BookingStatus.CANCELLED);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(cancelled));

            assertThatThrownBy(() ->
                bookingService.update(1L, null, LocalDate.now().plusDays(10), null, null))
                .isInstanceOf(IllegalStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("update invoca save exactamente una vez")
        void update_invocaSaveUnaVez() {
            BookingRequest draft = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(draft));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            bookingService.update(1L, null, LocalDate.now().plusDays(45), null, null);

            verify(bookingRequestPort, times(1)).save(any());
        }
    }

    // =========================================================================
    // delete() — campos conservados
    // =========================================================================

    @Nested
    @DisplayName("delete() — campos conservados")
    class DeleteFieldsTests {

        @Test
        @DisplayName("el booking eliminado conserva sus datos originales salvo active=false")
        void delete_conservaDatosOriginales() {
            BookingRequest draft = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(draft));

            bookingService.delete(1L);

            verify(bookingRequestPort).save(argThat(b ->
                !b.getActive()
                && b.getId().equals(draft.getId())
                && b.getBookingCode().equals(draft.getBookingCode())
                && b.getStatus() == BookingStatus.DRAFT
            ));
        }

        @Test
        @DisplayName("mensaje de InvalidBookingStateException al eliminar booking CONFIRMED")
        void delete_mensajeExcepcionConfirmed() {
            BookingRequest confirmed = buildSavedBooking(BookingStatus.CONFIRMED);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(confirmed));

            assertThatThrownBy(() -> bookingService.delete(1L))
                .isInstanceOf(InvalidBookingStateException.class)
                .hasMessageContaining("DRAFT");
        }

        @Test
        @DisplayName("mensaje de BookingNotFoundException contiene el id buscado")
        void delete_mensajeExcepcionContieneId() {
            when(bookingRequestPort.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.delete(42L))
                    .isInstanceOf(BookingNotFoundException.class)
                    .hasMessageContaining("42");
        }
    }

    // =========================================================================
    // findAll() y findWithFilters() — casos adicionales
    // =========================================================================

    @Nested
    @DisplayName("findAll() y findWithFilters() — casos adicionales")
    class FindExtendedTests {

        @Test
        @DisplayName("findAll retorna lista vacía cuando no hay bookings")
        void findAll_listaVacia() {
            when(bookingRequestPort.findAll()).thenReturn(List.of());

            List<BookingRequest> result = bookingService.findAll();

            assertThat(result).isEmpty();
            verify(bookingRequestPort).findAll();
        }

        @Test
        @DisplayName("findWithFilters con todos los parámetros null delega sin filtros")
        void findWithFilters_todosNull() {
            when(bookingRequestPort.findWithFilters(null, null, null, null, null, null))
                    .thenReturn(List.of());

            List<BookingRequest> result = bookingService.findWithFilters(
                    null, null, null, null, null, null
            );

            assertThat(result).isEmpty();
            verify(bookingRequestPort).findWithFilters(null, null, null, null, null, null);
        }

        @Test
        @DisplayName("findWithFilters con rango de fechas retorna los registros del periodo")
        void findWithFilters_conRangoFechas() {
            LocalDate desde = LocalDate.of(2026, 1, 1);
            LocalDate hasta = LocalDate.of(2026, 12, 31);
            List<BookingRequest> esperados = List.of(
                    buildSavedBooking(BookingStatus.CONFIRMED),
                    buildSavedBooking(BookingStatus.DRAFT)
            );
            when(bookingRequestPort.findWithFilters(null, null, null, desde, hasta, null))
                    .thenReturn(esperados);

            List<BookingRequest> result = bookingService.findWithFilters(
                    null, null, null, desde, hasta, null
            );

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("findWithFilters por freightMode AIR delega el parámetro correctamente")
        void findWithFilters_porFreightMode() {
            List<BookingRequest> aereos = List.of(buildSavedBooking(BookingStatus.DRAFT));
            when(bookingRequestPort.findWithFilters(null, null, FreightMode.AIR, null, null, null))
                    .thenReturn(aereos);

            List<BookingRequest> result = bookingService.findWithFilters(
                    null, null, FreightMode.AIR, null, null, null
            );

            assertThat(result).hasSize(1);
            verify(bookingRequestPort).findWithFilters(
                    null, null, FreightMode.AIR, null, null, null
            );
        }
    }

    // =========================================================================
    // changeStatus() — mensaje de excepción
    // =========================================================================

    @Nested
    @DisplayName("changeStatus() — mensajes de excepción")
    class ChangeStatusMessageTests {

        @Test
        @DisplayName("BookingNotFoundException al cambiar estado contiene el id")
        void changeStatus_mensajeExcepcionContieneId() {
            when(bookingRequestPort.findById(77L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.changeStatus(77L, BookingStatus.CONFIRMED))
                    .isInstanceOf(BookingNotFoundException.class)
                    .hasMessageContaining("77");
        }

        @Test
        @DisplayName("changeStatus invoca save exactamente una vez en transición válida")
        void changeStatus_invocaSaveUnaVez() {
            BookingRequest draft = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(draft));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            bookingService.changeStatus(1L, BookingStatus.CONFIRMED);

            verify(bookingRequestPort, times(1)).save(any());
        }
    }
}
