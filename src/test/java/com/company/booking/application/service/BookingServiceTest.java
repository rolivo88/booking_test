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

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService — pruebas unitarias")
class BookingServiceTest {

    @Mock
    private BookingRequestPort bookingRequestPort;

    @Mock
    private SupplierPort supplierPort;

    @InjectMocks
    private BookingService bookingService;

    // -------------------------------------------------------------------------
    // Fixtures reutilizables
    // -------------------------------------------------------------------------

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

    /** Construye un BookingRequest válido listo para ser creado. */
    private BookingRequest buildValidBooking() {
        return BookingRequest.builder()
                .bookingCode("BK-001")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(30))
                .currency("USD")
                .incotermCode(IncotermCode.FOB)
                .freightMode(FreightMode.SEA)
                .originCountry("China")
                .destinationCountry("Chile")
                .fobValue(new BigDecimal("5000.00"))
                .status(BookingStatus.DRAFT)
                .active(true)
                .items(List.of(item))
                .build();
    }

    /** Simula un BookingRequest ya persistido (con id). */
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
    // create()
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("crea correctamente un booking con supplier e ítems válidos")
        void create_ok() {
            BookingRequest input = buildValidBooking();
            BookingRequest saved = buildSavedBooking(BookingStatus.DRAFT);

            when(supplierPort.findByTaxId("12345678-9")).thenReturn(Optional.of(supplier));
            when(bookingRequestPort.save(any())).thenReturn(saved);

            BookingRequest result = bookingService.create(input, "12345678-9");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(BookingStatus.DRAFT);
            assertThat(result.getSupplier().getTaxId()).isEqualTo("12345678-9");
            verify(bookingRequestPort).save(any());
        }

        @Test
        @DisplayName("lanza SupplierNotFoundException cuando el taxId no existe")
        void create_supplierNotFound() {
            BookingRequest input = buildValidBooking();
            when(supplierPort.findByTaxId("INEXISTENTE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.create(input, "INEXISTENTE"))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessageContaining("INEXISTENTE");

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("lanza InvalidBookingStateException cuando el booking no tiene ítems")
        void create_sinItems() {
            BookingRequest sinItems = BookingRequest.builder()
                .bookingCode("BK-002")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(10))
                .currency("USD")
                .incotermCode(IncotermCode.CIF)
                .freightMode(FreightMode.AIR)
                .originCountry("China")
                .destinationCountry("Chile")
                .fobValue(new BigDecimal("1000.00"))
                .status(BookingStatus.DRAFT)
                .active(true)
                .items(List.of())   // sin ítems
                .build();

            when(supplierPort.findByTaxId("12345678-9")).thenReturn(Optional.of(supplier));

            assertThatThrownBy(() -> bookingService.create(sinItems, "12345678-9"))
                .isInstanceOf(InvalidBookingStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("lanza InvalidBookingStateException cuando la fecha de vencimiento ya pasó")
        void create_fechaVencida() {
            BookingRequest vencido = BookingRequest.builder()
                .bookingCode("BK-003")
                .issueDate(LocalDate.now().minusDays(60))
                .expirationDate(LocalDate.now().minusDays(1))  // en el pasado
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

            assertThatThrownBy(() -> bookingService.create(vencido, "12345678-9"))
                    .isInstanceOf(InvalidBookingStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("lanza InvalidBookingStateException cuando issueDate > expirationDate")
        void create_issueDatePosteriorAExpiration() {
            BookingRequest fechasInvalidas = BookingRequest.builder()
                .bookingCode("BK-004")
                .issueDate(LocalDate.now().plusDays(10))       // posterior
                .expirationDate(LocalDate.now().plusDays(5))   // anterior
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

            assertThatThrownBy(() -> bookingService.create(fechasInvalidas, "12345678-9"))
                .isInstanceOf(InvalidBookingStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("el estado inicial del booking creado siempre es DRAFT")
        void create_estadoInicialEsDraft() {
            BookingRequest input = buildValidBooking();
            when(supplierPort.findByTaxId("12345678-9")).thenReturn(Optional.of(supplier));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BookingRequest result = bookingService.create(input, "12345678-9");

            assertThat(result.getStatus()).isEqualTo(BookingStatus.DRAFT);
        }
    }

    // =========================================================================
    // findById() y findByBookingCode()
    // =========================================================================

    @Nested
    @DisplayName("findById() y findByBookingCode()")
    class FindByIdTests {

        @Test
        @DisplayName("retorna el booking cuando existe")
        void findById_encontrado() {
            BookingRequest saved = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(saved));

            Optional<BookingRequest> result = bookingService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando no existe")
        void findById_noEncontrado() {
            when(bookingRequestPort.findById(99L)).thenReturn(Optional.empty());

            Optional<BookingRequest> result = bookingService.findById(99L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findByBookingCode retorna el booking cuando el código existe")
        void findByBookingCode_encontrado() {
            BookingRequest saved = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findByBookingCode("BK-001")).thenReturn(Optional.of(saved));

            Optional<BookingRequest> result = bookingService.findByBookingCode("BK-001");

            assertThat(result).isPresent();
            assertThat(result.get().getBookingCode()).isEqualTo("BK-001");
        }

        @Test
        @DisplayName("findByBookingCode retorna Optional vacío cuando el código no existe")
        void findByBookingCode_noEncontrado() {
            when(bookingRequestPort.findByBookingCode("INEXISTENTE")).thenReturn(Optional.empty());

            Optional<BookingRequest> result = bookingService.findByBookingCode("INEXISTENTE");

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // changeStatus()
    // =========================================================================

    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatusTests {

        @Test
        @DisplayName("DRAFT → CONFIRMED exitosamente")
        void changeStatus_draftAConfirmed() {
            BookingRequest draft = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(draft));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BookingRequest result = bookingService.changeStatus(1L, BookingStatus.CONFIRMED);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            verify(bookingRequestPort).save(draft);
        }

        @Test
        @DisplayName("DRAFT → CANCELLED lanza IllegalStateException (cancel() solo aplica desde CONFIRMED)")
        void changeStatus_draftACancelled() {
            BookingRequest draft = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(draft));

            // El dominio solo permite cancel() desde CONFIRMED, no desde DRAFT
            assertThatThrownBy(() -> bookingService.changeStatus(1L, BookingStatus.CANCELLED))
                    .isInstanceOf(IllegalStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("CONFIRMED → CANCELLED exitosamente")
        void changeStatus_confirmedACancelled() {
            BookingRequest confirmed = buildSavedBooking(BookingStatus.CONFIRMED);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(confirmed));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BookingRequest result = bookingService.changeStatus(1L, BookingStatus.CANCELLED);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("lanza BookingNotFoundException cuando el booking no existe")
        void changeStatus_bookingNoEncontrado() {
            when(bookingRequestPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.changeStatus(99L, BookingStatus.CONFIRMED))
                .isInstanceOf(BookingNotFoundException.class);

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("CANCELLED → CONFIRMED lanza IllegalStateException (estado terminal)")
        void changeStatus_cancelledNoAdmiteTransicion() {
            BookingRequest cancelled = buildSavedBooking(BookingStatus.CANCELLED);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(cancelled));

            // CANCELLED no puede confirm() — lanza IllegalStateException desde el dominio
            assertThatThrownBy(() -> bookingService.changeStatus(1L, BookingStatus.CONFIRMED))
                    .isInstanceOf(IllegalStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("CONFIRMED → CONFIRMED lanza IllegalStateException (no es DRAFT)")
        void changeStatus_confirmedNoSePuedeConfirmarDeNuevo() {
            BookingRequest confirmed = buildSavedBooking(BookingStatus.CONFIRMED);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(confirmed));

            assertThatThrownBy(() -> bookingService.changeStatus(1L, BookingStatus.CONFIRMED))
                    .isInstanceOf(IllegalStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }
    }

    // =========================================================================
    // update()
    // =========================================================================

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("actualiza correctamente los campos de un booking DRAFT")
        void update_ok() {
            BookingRequest draft = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(draft));
            when(bookingRequestPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LocalDate nuevaExpiracion = LocalDate.now().plusDays(60);
            BigDecimal nuevoFob = new BigDecimal("9999.00");

            BookingRequest result = bookingService.update(
                    1L, null, nuevaExpiracion, nuevoFob, "EUR"
            );

            assertThat(result.getExpirationDate()).isEqualTo(nuevaExpiracion);
            assertThat(result.getFobValue()).isEqualByComparingTo(nuevoFob);
            assertThat(result.getCurrency()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("lanza BookingNotFoundException cuando el booking no existe")
        void update_bookingNoEncontrado() {
            when(bookingRequestPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    bookingService.update(99L, null, LocalDate.now().plusDays(10), null, null))
                    .isInstanceOf(BookingNotFoundException.class);
        }

        @Test
        @DisplayName("lanza IllegalStateException al intentar actualizar un booking CONFIRMED")
        void update_noPermitidoEnConfirmed() {
            BookingRequest confirmed = buildSavedBooking(BookingStatus.CONFIRMED);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(confirmed));

            assertThatThrownBy(() ->
                    bookingService.update(1L, null, LocalDate.now().plusDays(10), null, null))
                    .isInstanceOf(IllegalStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }
    }

    // =========================================================================
    // delete()
    // =========================================================================

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("elimina (soft) un booking en estado DRAFT")
        void delete_draft_ok() {
            BookingRequest draft = buildSavedBooking(BookingStatus.DRAFT);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(draft));

            bookingService.delete(1L);

            // Verifica que se guardó con active = false
            verify(bookingRequestPort).save(argThat(b -> !b.getActive()));
        }

        @Test
        @DisplayName("elimina (soft) un booking en estado CANCELLED")
        void delete_cancelled_ok() {
            BookingRequest cancelled = buildSavedBooking(BookingStatus.CANCELLED);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(cancelled));

            bookingService.delete(1L);

            verify(bookingRequestPort).save(argThat(b -> !b.getActive()));
        }

        @Test
        @DisplayName("lanza InvalidBookingStateException al eliminar un booking CONFIRMED")
        void delete_confirmed_noPermitido() {
            BookingRequest confirmed = buildSavedBooking(BookingStatus.CONFIRMED);
            when(bookingRequestPort.findById(1L)).thenReturn(Optional.of(confirmed));

            assertThatThrownBy(() -> bookingService.delete(1L))
                    .isInstanceOf(InvalidBookingStateException.class);

            verify(bookingRequestPort, never()).save(any());
        }

        @Test
        @DisplayName("lanza BookingNotFoundException cuando el booking no existe")
        void delete_bookingNoEncontrado() {
            when(bookingRequestPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.delete(99L))
                    .isInstanceOf(BookingNotFoundException.class);

            verify(bookingRequestPort, never()).save(any());
        }
    }

    // =========================================================================
    // findAll() y findWithFilters()
    // =========================================================================

    @Nested
    @DisplayName("findAll() y findWithFilters()")
    class FindTests {

        @Test
        @DisplayName("retorna la lista completa de bookings")
        void findAll_retornaLista() {
            List<BookingRequest> lista = List.of(
                    buildSavedBooking(BookingStatus.DRAFT),
                    buildSavedBooking(BookingStatus.CONFIRMED)
            );
            when(bookingRequestPort.findAll()).thenReturn(lista);

            List<BookingRequest> result = bookingService.findAll();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("delega los filtros correctamente al puerto de salida")
        void findWithFilters_delegaAlPuerto() {
            List<BookingRequest> filtrados = List.of(buildSavedBooking(BookingStatus.DRAFT));
            when(bookingRequestPort.findWithFilters(
                    "12345678-9", BookingStatus.DRAFT, FreightMode.SEA,
                    null, null, null
            )).thenReturn(filtrados);

            List<BookingRequest> result = bookingService.findWithFilters(
                    "12345678-9", BookingStatus.DRAFT, FreightMode.SEA,
                    null, null, null
            );

            assertThat(result).hasSize(1);
            verify(bookingRequestPort).findWithFilters(
                    "12345678-9", BookingStatus.DRAFT, FreightMode.SEA,
                    null, null, null
            );
        }
    }
}
