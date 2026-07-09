package com.company.booking.domain.model;

import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.model.enums.IncotermCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests del modelo de dominio BookingRequest.
 * No usa Spring ni Mockito — prueba la lógica pura del dominio.
 */
@DisplayName("BookingRequest — lógica de dominio y transiciones de estado")
class BookingRequestStateTest {

    private BookingItem item;

    @BeforeEach
    void setUp() {
        item = BookingItem.builder()
                .sku("SKU-001")
                .description("Producto")
                .quantity(5)
                .unitPrice(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("500.00"))
                .build();
    }

    private BookingRequest buildBooking(BookingStatus status) {
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
                .items(List.of(item))
                .build();
    }

    // =========================================================================
    // confirm()
    // =========================================================================

    @Nested
    @DisplayName("confirm()")
    class ConfirmTests {

        @Test
        @DisplayName("DRAFT → CONFIRMED cambia el estado correctamente")
        void confirm_draftACconfirmed() {
            BookingRequest booking = buildBooking(BookingStatus.DRAFT);

            booking.confirm();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("confirmar desde CONFIRMED lanza IllegalStateException")
        void confirm_desdeConfirmed_lanzaExcepcion() {
            BookingRequest booking = buildBooking(BookingStatus.CONFIRMED);

            assertThatThrownBy(booking::confirm)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("confirmar desde CANCELLED lanza IllegalStateException")
        void confirm_desdeCancelled_lanzaExcepcion() {
            BookingRequest booking = buildBooking(BookingStatus.CANCELLED);

            assertThatThrownBy(booking::confirm)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // cancel()
    // =========================================================================

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("CONFIRMED → CANCELLED cambia el estado correctamente")
        void cancel_confirmedACancelled() {
            BookingRequest booking = buildBooking(BookingStatus.CONFIRMED);

            booking.cancel();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancelar desde DRAFT lanza IllegalStateException (solo aplica desde CONFIRMED)")
        void cancel_desdeDraft_lanzaExcepcion() {
            BookingRequest booking = buildBooking(BookingStatus.DRAFT);

            assertThatThrownBy(booking::cancel)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("cancelar desde CANCELLED lanza IllegalStateException (estado terminal)")
        void cancel_desdeCancelled_lanzaExcepcion() {
            BookingRequest booking = buildBooking(BookingStatus.CANCELLED);

            assertThatThrownBy(booking::cancel)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // canUpdate() y canBeCancelled()
    // =========================================================================

    @Nested
    @DisplayName("canUpdate() y canBeCancelled()")
    class PermissionTests {

        @Test
        @DisplayName("canUpdate() retorna true solo en estado DRAFT")
        void canUpdate_soloEnDraft() {
            assertThat(buildBooking(BookingStatus.DRAFT).canUpdate()).isTrue();
            assertThat(buildBooking(BookingStatus.CONFIRMED).canUpdate()).isFalse();
            assertThat(buildBooking(BookingStatus.CANCELLED).canUpdate()).isFalse();
        }

        @Test
        @DisplayName("BookingStatus.canBeCancelled() retorna true solo en CONFIRMED")
        void canBeCancelled_soloEnConfirmed() {
            assertThat(BookingStatus.CONFIRMED.canBeCancelled()).isTrue();
            assertThat(BookingStatus.DRAFT.canBeCancelled()).isFalse();
            assertThat(BookingStatus.CANCELLED.canBeCancelled()).isFalse();
        }
    }

    // =========================================================================
    // isExpired()
    // =========================================================================

    @Nested
    @DisplayName("isExpired()")
    class ExpiredTests {

        @Test
        @DisplayName("retorna false cuando la fecha de vencimiento es futura")
        void isExpired_noVencido() {
            BookingRequest booking = buildBooking(BookingStatus.DRAFT);

            assertThat(booking.isExpired()).isFalse();
        }

        @Test
        @DisplayName("retorna true cuando la fecha de vencimiento está en el pasado")
        void isExpired_vencido() {
            BookingRequest vencido = BookingRequest.builder()
                    .id(2L)
                    .bookingCode("BK-EXP")
                    .issueDate(LocalDate.now().minusDays(60))
                    .expirationDate(LocalDate.now().minusDays(1))
                    .currency("USD")
                    .incotermCode(IncotermCode.CIF)
                    .freightMode(FreightMode.AIR)
                    .originCountry("China")
                    .destinationCountry("Chile")
                    .fobValue(new BigDecimal("1000.00"))
                    .status(BookingStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .active(true)
                    .items(List.of(item))
                    .build();

            assertThat(vencido.isExpired()).isTrue();
        }
    }

    // =========================================================================
    // hasItems()
    // =========================================================================

    @Nested
    @DisplayName("hasItems()")
    class HasItemsTests {

        @Test
        @DisplayName("retorna true cuando tiene ítems")
        void hasItems_conItems() {
            BookingRequest booking = buildBooking(BookingStatus.DRAFT);

            assertThat(booking.hasItems()).isTrue();
        }

        @Test
        @DisplayName("retorna false cuando la lista de ítems está vacía")
        void hasItems_sinItems() {
            BookingRequest sinItems = BookingRequest.builder()
                    .id(3L)
                    .bookingCode("BK-EMPTY")
                    .issueDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusDays(10))
                    .currency("USD")
                    .incotermCode(IncotermCode.EXW)
                    .freightMode(FreightMode.ROAD)
                    .originCountry("Argentina")
                    .destinationCountry("Chile")
                    .fobValue(new BigDecimal("200.00"))
                    .status(BookingStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .active(true)
                    .items(List.of())
                    .build();

            assertThat(sinItems.hasItems()).isFalse();
        }
    }

    // =========================================================================
    // withUpdatedFields()
    // =========================================================================

    @Nested
    @DisplayName("withUpdatedFields()")
    class UpdateFieldsTests {

        @Test
        @DisplayName("actualiza solo los campos no-null y mantiene los demás")
        void withUpdatedFields_actualizaParcialmente() {
            BookingRequest draft = buildBooking(BookingStatus.DRAFT);
            LocalDate nuevaExpiracion = LocalDate.now().plusDays(90);

            BookingRequest updated = draft.withUpdatedFields(
                    null, nuevaExpiracion, new BigDecimal("9000.00"), "EUR"
            );

            assertThat(updated.getExpirationDate()).isEqualTo(nuevaExpiracion);
            assertThat(updated.getFobValue()).isEqualByComparingTo("9000.00");
            assertThat(updated.getCurrency()).isEqualTo("EUR");
            // issueDate no cambió
            assertThat(updated.getIssueDate()).isEqualTo(draft.getIssueDate());
            // el resto de campos se mantiene
            assertThat(updated.getBookingCode()).isEqualTo(draft.getBookingCode());
            assertThat(updated.getStatus()).isEqualTo(BookingStatus.DRAFT);
        }

        @Test
        @DisplayName("lanza IllegalStateException al intentar actualizar desde CONFIRMED")
        void withUpdatedFields_noPermitidoEnConfirmed() {
            BookingRequest confirmed = buildBooking(BookingStatus.CONFIRMED);

            assertThatThrownBy(() ->
                    confirmed.withUpdatedFields(null, LocalDate.now().plusDays(10), null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando issueDate queda posterior a expirationDate")
        void withUpdatedFields_fechasInvalidas() {
            BookingRequest draft = buildBooking(BookingStatus.DRAFT);
            // issueDate actual es hoy; intentamos poner expirationDate ayer
            LocalDate ayer = LocalDate.now().minusDays(1);

            assertThatThrownBy(() ->
                    draft.withUpdatedFields(null, ayer, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("cuando todos los campos son null retorna una instancia con los mismos valores")
        void withUpdatedFields_todosNullConservaValores() {
            BookingRequest draft = buildBooking(BookingStatus.DRAFT);

            BookingRequest result = draft.withUpdatedFields(null, null, null, null);

            assertThat(result.getIssueDate()).isEqualTo(draft.getIssueDate());
            assertThat(result.getExpirationDate()).isEqualTo(draft.getExpirationDate());
            assertThat(result.getFobValue()).isEqualByComparingTo(draft.getFobValue());
            assertThat(result.getCurrency()).isEqualTo(draft.getCurrency());
        }

        @Test
        @DisplayName("retorna una nueva instancia (no modifica el objeto original)")
        void withUpdatedFields_retornaInstanciaNueva() {
            BookingRequest draft = buildBooking(BookingStatus.DRAFT);
            LocalDate nuevaExpiracion = LocalDate.now().plusDays(90);

            BookingRequest updated = draft.withUpdatedFields(null, nuevaExpiracion, null, null);

            // Distintas referencias
            assertThat(updated).isNotSameAs(draft);
            // El original no cambió
            assertThat(draft.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(30));
        }

        @Test
        @DisplayName("issueDate igual a expirationDate es válido (caso borde)")
        void withUpdatedFields_issueDateIgualAExpiration_esValido() {
            BookingRequest draft = buildBooking(BookingStatus.DRAFT);
            LocalDate mismaFecha = LocalDate.now().plusDays(30); // igual que expirationDate actual

            // No debe lanzar excepción
            BookingRequest result = draft.withUpdatedFields(mismaFecha, mismaFecha, null, null);

            assertThat(result.getIssueDate()).isEqualTo(mismaFecha);
            assertThat(result.getExpirationDate()).isEqualTo(mismaFecha);
        }

        @Test
        @DisplayName("lanza IllegalStateException al intentar actualizar desde CANCELLED")
        void withUpdatedFields_noPermitidoEnCancelled() {
            BookingRequest cancelled = buildBooking(BookingStatus.CANCELLED);

            assertThatThrownBy(() ->
                    cancelled.withUpdatedFields(null, LocalDate.now().plusDays(10), null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // BookingStatus — canBeModified()
    // =========================================================================

    @Nested
    @DisplayName("BookingStatus.canBeModified()")
    class BookingStatusTests {

        @Test
        @DisplayName("DRAFT puede ser modificado")
        void canBeModified_draft() {
            assertThat(BookingStatus.DRAFT.canBeModified()).isTrue();
        }

        @Test
        @DisplayName("CONFIRMED no puede ser modificado")
        void canBeModified_confirmed() {
            assertThat(BookingStatus.CONFIRMED.canBeModified()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED no puede ser modificado")
        void canBeModified_cancelled() {
            assertThat(BookingStatus.CANCELLED.canBeModified()).isFalse();
        }
    }

    // =========================================================================
    // Integridad de campos tras confirm() y cancel()
    // =========================================================================

    @Nested
    @DisplayName("Integridad de campos tras transiciones")
    class FieldIntegrityTests {

        @Test
        @DisplayName("confirm() no modifica otros campos del booking")
        void confirm_soloModificaStatus() {
            BookingRequest booking = buildBooking(BookingStatus.DRAFT);
            String codigoOriginal = booking.getBookingCode();
            BigDecimal fobOriginal = booking.getFobValue();

            booking.confirm();

            assertThat(booking.getBookingCode()).isEqualTo(codigoOriginal);
            assertThat(booking.getFobValue()).isEqualByComparingTo(fobOriginal);
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("cancel() no modifica otros campos del booking")
        void cancel_soloModificaStatus() {
            BookingRequest booking = buildBooking(BookingStatus.CONFIRMED);
            String codigoOriginal = booking.getBookingCode();

            booking.cancel();

            assertThat(booking.getBookingCode()).isEqualTo(codigoOriginal);
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }
    }
}
