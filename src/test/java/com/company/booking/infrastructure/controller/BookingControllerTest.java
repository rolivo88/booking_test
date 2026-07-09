package com.company.booking.infrastructure.controller;

import com.company.booking.domain.exception.BookingNotFoundException;
import com.company.booking.domain.exception.InvalidBookingStateException;
import com.company.booking.domain.exception.SupplierNotFoundException;
import com.company.booking.domain.model.BookingItem;
import com.company.booking.domain.model.BookingRequest;
import com.company.booking.domain.model.Supplier;
import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.model.enums.IncotermCode;
import com.company.booking.domain.port.in.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@DisplayName("BookingController — pruebas de capa web")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CreateBookingUseCase createBookingUseCase;
    @MockitoBean private FindBookingUseCase findBookingUseCase;
    @MockitoBean private FindBookingWithFiltersUseCase findBookingWithFiltersUseCase;
    @MockitoBean private UpdateBookingUseCase updateBookingUseCase;
    @MockitoBean private ChangeBookingStatusUseCase changeBookingStatusUseCase;
    @MockitoBean private DeleteBookingUseCase deleteBookingUseCase;

    private ObjectMapper objectMapper;
    private BookingRequest savedBooking;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Supplier supplier = Supplier.builder()
                .id(1L).name("Proveedor SA").taxId("12345678-9")
                .country("Chile").contactEmail("p@test.com")
                .createdAt(LocalDateTime.now()).build();

        BookingItem item = BookingItem.builder()
                .id(10L).sku("SKU-001").description("Producto")
                .quantity(5).unitPrice(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("500.00")).build();

        savedBooking = BookingRequest.builder()
                .id(1L).bookingCode("BK-001")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(30))
                .currency("USD").incotermCode(IncotermCode.FOB).freightMode(FreightMode.SEA)
                .originCountry("China").destinationCountry("Chile")
                .fobValue(new BigDecimal("5000.00"))
                .status(BookingStatus.DRAFT)
                .createdAt(LocalDateTime.now()).active(true)
                .supplier(supplier).items(List.of(item)).build();
    }

    // =========================================================================
    // GET /api/bookings
    // =========================================================================

    @Nested
    @DisplayName("GET /api/bookings")
    class GetAllTests {

        @Test
        @DisplayName("retorna 200 y lista de bookings")
        void getAll_ok() throws Exception {
            when(findBookingWithFiltersUseCase.findWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(savedBooking));

            mockMvc.perform(get("/api/bookings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].bookingCode").value("BK-001"))
                    .andExpect(jsonPath("$[0].status").value("DRAFT"));
        }

        @Test
        @DisplayName("acepta filtro por status y retorna 200")
        void getAll_conFiltroStatus() throws Exception {
            when(findBookingWithFiltersUseCase.findWithFilters(any(), eq(BookingStatus.DRAFT), any(), any(), any(), any()))
                    .thenReturn(List.of(savedBooking));

            mockMvc.perform(get("/api/bookings").param("status", "DRAFT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("DRAFT"));
        }

        @Test
        @DisplayName("retorna 200 con lista vacía cuando no hay bookings")
        void getAll_listaVacia() throws Exception {
            when(findBookingWithFiltersUseCase.findWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/bookings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // =========================================================================
    // GET /api/bookings/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/bookings/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("retorna 200 con el booking encontrado")
        void getById_ok() throws Exception {
            when(findBookingUseCase.findById(1L)).thenReturn(Optional.of(savedBooking));

            mockMvc.perform(get("/api/bookings/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.bookingCode").value("BK-001"))
                    .andExpect(jsonPath("$.supplier.taxId").value("12345678-9"))
                    .andExpect(jsonPath("$.items[0].sku").value("SKU-001"));
        }

        @Test
        @DisplayName("retorna 404 cuando el booking no existe")
        void getById_notFound() throws Exception {
            when(findBookingUseCase.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/bookings/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // POST /api/bookings
    // =========================================================================

    @Nested
    @DisplayName("POST /api/bookings")
    class PostTests {

        private String validBody() {
            return """
                {
                  "bookingCode": "BK-001",
                  "issueDate": "2026-07-01",
                  "expirationDate": "2026-12-31",
                  "currency": "USD",
                  "incotermCode": "FOB",
                  "freightMode": "SEA",
                  "originCountry": "China",
                  "destinationCountry": "Chile",
                  "fobValue": 5000.00,
                  "supplierTaxId": "12345678-9",
                  "items": [
                    {
                      "sku": "SKU-001",
                      "description": "Producto",
                      "quantity": 5,
                      "unitPrice": 100.00
                    }
                  ]
                }
                """;
        }

        @Test
        @DisplayName("retorna 201 al crear booking exitosamente")
        void post_ok() throws Exception {
            when(createBookingUseCase.create(any(), any())).thenReturn(savedBooking);

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.bookingCode").value("BK-001"));
        }

        @Test
        @DisplayName("retorna 404 cuando el proveedor no existe")
        void post_supplierNotFound() throws Exception {
            when(createBookingUseCase.create(any(), any()))
                    .thenThrow(new SupplierNotFoundException("NOPE"));

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("NOPE")));
        }

        @Test
        @DisplayName("retorna 422 cuando no se cumplen reglas de negocio")
        void post_reglaNegocio() throws Exception {
            when(createBookingUseCase.create(any(), any()))
                    .thenThrow(new InvalidBookingStateException("El booking debe contener al menos un ítem"));

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }

        @Test
        @DisplayName("retorna 400 cuando el body tiene campos inválidos")
        void post_validacionFallida() throws Exception {
            String bodyInvalido = """
                {
                  "bookingCode": "",
                  "currency": "USD",
                  "incotermCode": "FOB",
                  "freightMode": "SEA",
                  "originCountry": "China",
                  "destinationCountry": "Chile",
                  "fobValue": 100.00,
                  "supplierTaxId": "12345678-9",
                  "items": [{"sku":"S","description":"D","quantity":1,"unitPrice":10.0}]
                }
                """;

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyInvalido))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray());
        }
    }

    // =========================================================================
    // PATCH /api/bookings/{id}
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/bookings/{id}")
    class PatchUpdateTests {

        @Test
        @DisplayName("retorna 200 al actualizar campos correctamente")
        void patch_ok() throws Exception {
            when(updateBookingUseCase.update(eq(1L), any(), any(), any(), any()))
                    .thenReturn(savedBooking);

            mockMvc.perform(patch("/api/bookings/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fobValue\": 9000.00}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("retorna 404 cuando el booking no existe")
        void patch_notFound() throws Exception {
            when(updateBookingUseCase.update(eq(99L), any(), any(), any(), any()))
                    .thenThrow(new BookingNotFoundException(99L));

            mockMvc.perform(patch("/api/bookings/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"currency\": \"EUR\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna 422 al actualizar un booking no DRAFT")
        void patch_estadoInvalido() throws Exception {
            when(updateBookingUseCase.update(eq(1L), any(), any(), any(), any()))
                    .thenThrow(new IllegalStateException("Solo los bookings en estado DRAFT pueden ser modificados"));

            mockMvc.perform(patch("/api/bookings/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"currency\": \"EUR\"}"))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // =========================================================================
    // PATCH /api/bookings/{id}/status
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/bookings/{id}/status")
    class PatchStatusTests {

        @Test
        @DisplayName("retorna 200 al cambiar estado exitosamente")
        void patchStatus_ok() throws Exception {
            BookingRequest confirmed = BookingRequest.builder()
                    .id(1L).bookingCode("BK-001")
                    .issueDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(30))
                    .currency("USD").incotermCode(IncotermCode.FOB).freightMode(FreightMode.SEA)
                    .originCountry("China").destinationCountry("Chile")
                    .fobValue(new BigDecimal("5000.00")).status(BookingStatus.CONFIRMED)
                    .createdAt(LocalDateTime.now()).active(true)
                    .supplier(savedBooking.getSupplier()).items(savedBooking.getItems()).build();

            when(changeBookingStatusUseCase.changeStatus(1L, BookingStatus.CONFIRMED))
                    .thenReturn(confirmed);

            mockMvc.perform(patch("/api/bookings/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"CONFIRMED\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("retorna 422 en transición inválida")
        void patchStatus_transicionInvalida() throws Exception {
            when(changeBookingStatusUseCase.changeStatus(eq(1L), any()))
                    .thenThrow(new IllegalStateException("Solo los bookings en estado DRAFT pueden ser confirmados"));

            mockMvc.perform(patch("/api/bookings/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"CONFIRMED\"}"))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("retorna 404 cuando el booking no existe")
        void patchStatus_notFound() throws Exception {
            when(changeBookingStatusUseCase.changeStatus(eq(99L), any()))
                    .thenThrow(new BookingNotFoundException(99L));

            mockMvc.perform(patch("/api/bookings/99/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"CONFIRMED\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna 400 cuando el status es null")
        void patchStatus_statusNull() throws Exception {
            mockMvc.perform(patch("/api/bookings/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": null}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // DELETE /api/bookings/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/bookings/{id}")
    class DeleteTests {

        @Test
        @DisplayName("retorna 204 al eliminar exitosamente")
        void delete_ok() throws Exception {
            doNothing().when(deleteBookingUseCase).delete(1L);

            mockMvc.perform(delete("/api/bookings/1"))
                    .andExpect(status().isNoContent());

            verify(deleteBookingUseCase).delete(1L);
        }

        @Test
        @DisplayName("retorna 404 cuando el booking no existe")
        void delete_notFound() throws Exception {
            doThrow(new BookingNotFoundException(99L)).when(deleteBookingUseCase).delete(99L);

            mockMvc.perform(delete("/api/bookings/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna 422 al eliminar booking CONFIRMED")
        void delete_estadoInvalido() throws Exception {
            doThrow(new InvalidBookingStateException("Solo se pueden eliminar bookings en estado DRAFT o CANCELADO"))
                    .when(deleteBookingUseCase).delete(1L);

            mockMvc.perform(delete("/api/bookings/1"))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // =========================================================================
    // Excepciones — BookingNotFoundException constructor(String)
    // =========================================================================

    @Nested
    @DisplayName("BookingNotFoundException — ambos constructores")
    class BookingNotFoundExceptionTests {

        @Test
        @DisplayName("constructor(Long) genera mensaje con id")
        void constructor_long() {
            var ex = new BookingNotFoundException(42L);
            org.assertj.core.api.Assertions.assertThat(ex.getMessage()).contains("42");
        }

        @Test
        @DisplayName("constructor(String) genera mensaje con bookingCode")
        void constructor_string() {
            var ex = new BookingNotFoundException("BK-999");
            org.assertj.core.api.Assertions.assertThat(ex.getMessage()).contains("BK-999");
        }
    }
}
