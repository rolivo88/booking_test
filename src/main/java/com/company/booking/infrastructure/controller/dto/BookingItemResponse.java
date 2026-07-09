package com.company.booking.infrastructure.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@Schema(description = "Ítem incluido en la respuesta del booking")
public class BookingItemResponse {

    @Schema(description = "ID del ítem", example = "1")
    Long id;

    @Schema(description = "Código SKU del producto", example = "SKU-PROD-001")
    String sku;

    @Schema(description = "Descripción del producto", example = "Carcasa plástica para dispositivo eléctrico")
    String description;

    @Schema(description = "Cantidad de unidades", example = "100")
    Integer quantity;

    @Schema(description = "Precio unitario", example = "12.50")
    BigDecimal unitPrice;

    @Schema(description = "Total calculado (quantity × unitPrice)", example = "1250.00")
    BigDecimal totalAmount;
}
