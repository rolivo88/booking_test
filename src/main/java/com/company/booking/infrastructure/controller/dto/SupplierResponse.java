package com.company.booking.infrastructure.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Datos del proveedor asociado al booking")
public class SupplierResponse {

    @Schema(description = "ID del proveedor", example = "1")
    Long id;

    @Schema(description = "Nombre o razón social del proveedor", example = "Shenzhen Electronics Co.")
    String name;

    @Schema(description = "RUT o Tax ID del proveedor", example = "12345678-9")
    String taxId;

    @Schema(description = "País del proveedor", example = "China")
    String country;

    @Schema(description = "Email de contacto", example = "contact@shenzhen-elec.com")
    String contactEmail;
}
