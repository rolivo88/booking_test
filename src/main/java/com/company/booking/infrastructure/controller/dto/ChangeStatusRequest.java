package com.company.booking.infrastructure.controller.dto;

import com.company.booking.domain.model.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Nuevo estado al que se desea transicionar el booking")
public class ChangeStatusRequest {

    @Schema(
        description = "Estado destino. Transiciones válidas: DRAFT→CONFIRMED, CONFIRMED→CANCELLED",
        example = "CONFIRMED",
        allowableValues = {"CONFIRMED", "CANCELLED"}
    )
    @NotNull(message = "El estado es obligatorio")
    BookingStatus status;
}
