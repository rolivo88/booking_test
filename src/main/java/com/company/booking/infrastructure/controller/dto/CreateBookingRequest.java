package com.company.booking.infrastructure.controller.dto;

import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.model.enums.IncotermCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
@Schema(description = "Datos para crear una nueva solicitud de embarque")
public class CreateBookingRequest {

    @Schema(description = "Código único de referencia del booking", example = "BK-2026-001", maxLength = 50)
    @NotBlank(message = "El código de booking es obligatorio")
    @Size(max = 50, message = "El código de booking no debe superar los 50 caracteres")
    String bookingCode;

    @Schema(description = "Fecha de emisión del booking", example = "2026-07-01")
    @NotNull(message = "La fecha de emisión es obligatoria")
    LocalDate issueDate;

    @Schema(description = "Fecha de vencimiento del booking (debe ser futura)", example = "2026-12-31")
    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser una fecha futura")
    LocalDate expirationDate;

    @Schema(description = "Moneda de la operación", example = "USD", maxLength = 10)
    @NotBlank(message = "La moneda es obligatoria")
    @Size(max = 10, message = "La moneda no debe superar los 10 caracteres")
    String currency;

    @Schema(description = "Código Incoterm de la negociación", example = "FOB")
    @NotNull(message = "El código incoterm es obligatorio")
    IncotermCode incotermCode;

    @Schema(description = "Modalidad de transporte del embarque", example = "SEA")
    @NotNull(message = "La modalidad de flete es obligatoria")
    FreightMode freightMode;

    @Schema(description = "País de origen del embarque", example = "China", maxLength = 100)
    @NotBlank(message = "El país de origen es obligatorio")
    @Size(max = 100, message = "El país de origen no debe superar los 100 caracteres")
    String originCountry;

    @Schema(description = "País de destino del embarque", example = "Chile", maxLength = 100)
    @NotBlank(message = "El país de destino es obligatorio")
    @Size(max = 100, message = "El país de destino no debe superar los 100 caracteres")
    String destinationCountry;

    @Schema(description = "Valor FOB total de la operación", example = "15000.00")
    @NotNull(message = "El valor FOB es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor FOB debe ser mayor a 0")
    BigDecimal fobValue;

    @Schema(description = "Tax ID / RUT del proveedor (debe existir en el sistema)", example = "12345678-9")
    @NotBlank(message = "El Tax ID del proveedor es obligatorio")
    String supplierTaxId;

    @Schema(description = "Lista de ítems del embarque (mínimo 1)")
    @NotNull(message = "Los ítems son obligatorios")
    @Size(min = 1, message = "El booking debe tener al menos un ítem")
    @Valid
    List<BookingItemRequest> items;
}
