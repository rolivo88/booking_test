package com.company.booking.infrastructure.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Schema(description = """
        Campos actualizables de una solicitud de embarque.
        Solo aplica cuando el booking está en estado DRAFT.
        Los campos con valor null se ignoran (no se modifican).
        """)
public class UpdateBookingRequest {

    @Schema(description = "Nueva fecha de emisión", example = "2026-08-01", nullable = true)
    LocalDate issueDate;

    @Schema(description = "Nueva fecha de vencimiento (debe ser futura)", example = "2027-01-31", nullable = true)
    @Future(message = "La fecha de vencimiento debe ser una fecha futura")
    LocalDate expirationDate;

    @Schema(description = "Nuevo valor FOB (mayor a 0)", example = "20000.00", nullable = true)
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor FOB debe ser mayor a 0")
    BigDecimal fobValue;

    @Schema(description = "Nueva moneda", example = "EUR", maxLength = 10, nullable = true)
    @Size(max = 10, message = "La moneda no debe superar los 10 caracteres")
    String currency;
}
