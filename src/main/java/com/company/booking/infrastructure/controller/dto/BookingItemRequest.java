package com.company.booking.infrastructure.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@Schema(description = "Ítem individual dentro de una solicitud de embarque")
public class BookingItemRequest {

    @Schema(description = "Código de producto (SKU)", example = "SKU-PROD-001")
    @NotBlank(message = "El SKU es obligatorio")
    String sku;

    @Schema(description = "Descripción del producto", example = "Carcasa plástica para dispositivo eléctrico")
    @NotBlank(message = "La descripción es obligatoria")
    String description;

    @Schema(description = "Cantidad de unidades (mínimo 1)", example = "100")
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    Integer quantity;

    @Schema(description = "Precio unitario del producto (mayor a 0)", example = "12.50")
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
    BigDecimal unitPrice;
}
