package com.company.booking.infrastructure.controller;

import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.port.in.ChangeBookingStatusUseCase;
import com.company.booking.domain.port.in.CreateBookingUseCase;
import com.company.booking.domain.port.in.DeleteBookingUseCase;
import com.company.booking.domain.port.in.FindBookingUseCase;
import com.company.booking.domain.port.in.FindBookingWithFiltersUseCase;
import com.company.booking.domain.port.in.UpdateBookingUseCase;
import com.company.booking.infrastructure.controller.dto.BookingDtoMapper;
import com.company.booking.infrastructure.controller.dto.BookingResponse;
import com.company.booking.infrastructure.controller.dto.ChangeStatusRequest;
import com.company.booking.infrastructure.controller.dto.CreateBookingRequest;
import com.company.booking.infrastructure.controller.dto.UpdateBookingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(
    name = "Booking Requests",
    description = "Gestión de solicitudes de embarque. Permite crear, consultar, actualizar, " +
                  "cambiar estado y eliminar booking requests."
)
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final FindBookingUseCase findBookingUseCase;
    private final FindBookingWithFiltersUseCase findBookingWithFiltersUseCase;
    private final UpdateBookingUseCase updateBookingUseCase;
    private final ChangeBookingStatusUseCase changeBookingStatusUseCase;
    private final DeleteBookingUseCase deleteBookingUseCase;

    public BookingController(
            CreateBookingUseCase createBookingUseCase,
            FindBookingUseCase findBookingUseCase,
            FindBookingWithFiltersUseCase findBookingWithFiltersUseCase,
            UpdateBookingUseCase updateBookingUseCase,
            ChangeBookingStatusUseCase changeBookingStatusUseCase,
            DeleteBookingUseCase deleteBookingUseCase
    ) {
        this.createBookingUseCase          = createBookingUseCase;
        this.findBookingUseCase            = findBookingUseCase;
        this.findBookingWithFiltersUseCase = findBookingWithFiltersUseCase;
        this.updateBookingUseCase          = updateBookingUseCase;
        this.changeBookingStatusUseCase    = changeBookingStatusUseCase;
        this.deleteBookingUseCase          = deleteBookingUseCase;
    }

    // -------------------------------------------------------------------------
    // 1. GET /api/bookings
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Listar booking requests con filtros",
        description = """
            Retorna la lista de booking requests activos (active = true).
            Todos los filtros son opcionales y combinables entre sí.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista obtenida exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = BookingResponse.class))
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<BookingResponse>> findAll(
            @Parameter(description = "RUT o Tax ID del proveedor")
            @RequestParam(required = false) String taxId,

            @Parameter(description = "Estado del booking", schema = @Schema(allowableValues = {"DRAFT", "CONFIRMED", "CANCELLED"}))
            @RequestParam(required = false) BookingStatus status,

            @Parameter(description = "Modalidad de flete", schema = @Schema(allowableValues = {"AIR", "SEA", "ROAD"}))
            @RequestParam(required = false) FreightMode freightMode,

            @Parameter(description = "Fecha de emisión desde (formato: yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

            @Parameter(description = "Fecha de emisión hasta (formato: yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,

            @Parameter(description = "Búsqueda exacta por código de booking", example = "BK-2026-001")
            @RequestParam(required = false) String bookingCode
    ) {
        List<BookingResponse> response = findBookingWithFiltersUseCase
                .findWithFilters(taxId, status, freightMode, dateFrom, dateTo, bookingCode)
                .stream()
                .map(BookingDtoMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // 2. POST /api/bookings
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Crear booking request",
        description = """
            Crea una nueva solicitud de embarque en estado **DRAFT**.
            
            - El proveedor se referencia por `supplierTaxId` (debe existir en la base de datos).
            - `totalAmount` de cada ítem se calcula automáticamente en el backend (`quantity × unitPrice`).
            - `issueDate` debe ser menor o igual a `expirationDate`.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Booking creado exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BookingResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (validación fallida)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado con el taxId indicado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "422", description = "Regla de negocio no cumplida (sin ítems, fecha vencida, etc.)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de la solicitud de embarque",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateBookingRequest.class))
            )
            @Valid @RequestBody CreateBookingRequest request
    ) {
        BookingResponse response = BookingDtoMapper.toResponse(
                createBookingUseCase.create(
                        BookingDtoMapper.toDomain(request),
                        request.getSupplierTaxId()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------------------------------------------------------------
    // 3. GET /api/bookings/{id}
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Obtener booking request por ID",
        description = "Retorna el detalle completo de un booking: cabecera, proveedor e ítems."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Booking encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BookingResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Booking no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> findById(
            @Parameter(description = "ID del booking", required = true, example = "1")
            @PathVariable Long id
    ) {
        return findBookingUseCase.findById(id)
                .map(b -> ResponseEntity.ok(BookingDtoMapper.toResponse(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------------------------------------------------------------
    // 4. PATCH /api/bookings/{id}
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Actualizar booking request",
        description = """
            Actualiza parcialmente los campos permitidos de un booking.
            
            **Solo aplica cuando el estado es DRAFT.**
            
            Campos modificables:
            - `issueDate` — fecha de emisión
            - `expirationDate` — fecha de vencimiento (debe ser futura)
            - `fobValue` — valor FOB
            - `currency` — moneda
            
            Los campos con valor `null` en el body se ignoran (no se modifican).
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Booking actualizado exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BookingResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "404", description = "Booking no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "422", description = "El booking no está en estado DRAFT o las fechas son inválidas",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<BookingResponse> update(
            @Parameter(description = "ID del booking", required = true, example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Campos a actualizar (solo los no-null se aplican)",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateBookingRequest.class))
            )
            @Valid @RequestBody UpdateBookingRequest request
    ) {
        BookingResponse response = BookingDtoMapper.toResponse(
                updateBookingUseCase.update(
                        id,
                        request.getIssueDate(),
                        request.getExpirationDate(),
                        request.getFobValue(),
                        request.getCurrency()
                )
        );
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // 5. PATCH /api/bookings/{id}/status
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Cambiar estado del booking",
        description = """
            Realiza una transición de estado sobre el booking.
            
            **Transiciones válidas:**
            
            | Desde | Hacia |
            |-------|-------|
            | DRAFT | CONFIRMED |
            | CONFIRMED | CANCELLED |
            
            `CANCELLED` es un **estado terminal** — no admite ninguna transición adicional.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BookingResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Booking no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "422", description = "Transición de estado no permitida",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponse> changeStatus(
            @Parameter(description = "ID del booking", required = true, example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Nuevo estado al que se desea transicionar",
                required = true,
                content = @Content(schema = @Schema(implementation = ChangeStatusRequest.class))
            )
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        BookingResponse response = BookingDtoMapper.toResponse(
                changeBookingStatusUseCase.changeStatus(id, request.getStatus())
        );
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // 6. DELETE /api/bookings/{id}
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Eliminar booking request (soft delete)",
        description = """
            Marca el booking como inactivo (`active = false`). **No elimina el registro físicamente.**
            
            Solo se permite si el estado es **DRAFT** o **CANCELLED**.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Booking eliminado exitosamente (sin contenido)"),
        @ApiResponse(responseCode = "404", description = "Booking no encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "422", description = "No se puede eliminar un booking en estado CONFIRMED",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del booking", required = true, example = "1")
            @PathVariable Long id
    ) {
        deleteBookingUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
