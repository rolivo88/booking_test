package com.company.booking.infrastructure.controller.dto;

import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.model.enums.IncotermCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@Schema(description = "Respuesta completa de una solicitud de embarque")
public class BookingResponse {

    @Schema(description = "ID único del booking", example = "1")
    Long id;

    @Schema(description = "Código de referencia único", example = "BK-2026-001")
    String bookingCode;

    @Schema(description = "Fecha de emisión", example = "2026-07-01")
    LocalDate issueDate;

    @Schema(description = "Fecha de vencimiento", example = "2026-12-31")
    LocalDate expirationDate;

    @Schema(description = "Moneda de la operación", example = "USD")
    String currency;

    @Schema(description = "Código Incoterm", example = "FOB")
    IncotermCode incotermCode;

    @Schema(description = "Modalidad de transporte", example = "SEA")
    FreightMode freightMode;

    @Schema(description = "País de origen", example = "China")
    String originCountry;

    @Schema(description = "País de destino", example = "Chile")
    String destinationCountry;

    @Schema(description = "Valor FOB total", example = "15000.00")
    BigDecimal fobValue;

    @Schema(description = "Estado actual del booking", example = "DRAFT")
    BookingStatus status;

    @Schema(description = "Fecha y hora de creación (generada automáticamente)")
    LocalDateTime createdAt;

    @Schema(description = "Indica si el registro está activo (false = eliminado lógicamente)", example = "true")
    Boolean active;

    @Schema(description = "Datos del proveedor asociado")
    SupplierResponse supplier;

    @Schema(description = "Lista de ítems del embarque")
    List<BookingItemResponse> items;
}
