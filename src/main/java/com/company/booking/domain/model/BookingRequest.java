package com.company.booking.domain.model;

import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.domain.model.enums.IncotermCode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
public class BookingRequest {


    private final Long id;
    private final String bookingCode;
    private final LocalDate issueDate;
    private final LocalDate expirationDate;
    private final String currency;
    private final IncotermCode incotermCode;
    private final FreightMode freightMode;
    private final String originCountry;
    private final String destinationCountry;
    private final BigDecimal fobValue;

    /**
     * Estado controlado mediante reglas de negocio.
     */
    private BookingStatus status;
    private final LocalDateTime createdAt;
    private Boolean active;
    private final Supplier supplier;
    private final List<BookingItem> items;

    /**
     * Constructor explícito para evitar problemas
     * con Lombok Builder + campos final.
     */
    public BookingRequest(
        Long id,
        String bookingCode,
        LocalDate issueDate,
        LocalDate expirationDate,
        String currency,
        IncotermCode incotermCode,
        FreightMode freightMode,
        String originCountry,
        String destinationCountry,
        BigDecimal fobValue,
        BookingStatus status,
        LocalDateTime createdAt,
        Boolean active,
        Supplier supplier,
        List<BookingItem> items
    ) {

        this.id = id;
        this.bookingCode = bookingCode;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.currency = currency;
        this.incotermCode = incotermCode;
        this.freightMode = freightMode;
        this.originCountry = originCountry;
        this.destinationCountry = destinationCountry;
        this.fobValue = fobValue;
        this.status = status;
        this.createdAt = createdAt;
        this.active = active;
        this.supplier = supplier;

        this.items = items == null
            ? List.of()
            : List.copyOf(items);

    }

    /**
     * Valida si la solicitud ya expiró.
     */
    public boolean isExpired(){
        return expirationDate.isBefore(
            LocalDate.now()
        );

    }



    /**
     * Determina si puede ser modificada.
     */
    public boolean canUpdate(){

        return status.canBeModified();

    }



    /**
     * Confirma una solicitud.
     */
    public void confirm(){

        if(status != BookingStatus.DRAFT){

            throw new IllegalStateException(
                "Solo los bookings en estado DRAFT pueden ser confirmados"
            );

        }
        status = BookingStatus.CONFIRMED;
    }



    /**
     * Cancela una solicitud confirmada.
     */
    public void cancel(){

        if(!status.canBeCancelled()){

            throw new IllegalStateException(
                "Only confirmed bookings can be cancelled"
            );

        }


        status = BookingStatus.CANCELLED;

    }



    /**
     * Regla de negocio:
     * una solicitud válida debe tener items.
     */
    public boolean hasItems(){

        return !items.isEmpty();

    }


    /**
     * Actualiza los campos permitidos cuando el estado es DRAFT.
     * Reglas:
     * - Solo se modifica un campo si el valor nuevo no es null.
     * - issueDate debe ser <= expirationDate resultante.
     * Retorna una nueva instancia (immutable rebuild).
     */
    public BookingRequest withUpdatedFields(
        LocalDate newIssueDate,
        LocalDate newExpirationDate,
        java.math.BigDecimal newFobValue,
        String newCurrency
    ) {
        if (!canUpdate()) {
            throw new IllegalStateException(
                "Only DRAFT bookings can be updated"
            );
        }

        LocalDate resolvedIssueDate      = newIssueDate      != null ? newIssueDate      : this.issueDate;
        LocalDate resolvedExpirationDate = newExpirationDate != null ? newExpirationDate : this.expirationDate;

        if (resolvedIssueDate.isAfter(resolvedExpirationDate)) {
            throw new IllegalArgumentException(
                "issueDate must be before or equal to expirationDate"
            );
        }

        return BookingRequest.builder()
            .id(this.id)
            .bookingCode(this.bookingCode)
            .issueDate(resolvedIssueDate)
            .expirationDate(resolvedExpirationDate)
            .currency(newCurrency != null ? newCurrency : this.currency)
            .incotermCode(this.incotermCode)
            .freightMode(this.freightMode)
            .originCountry(this.originCountry)
            .destinationCountry(this.destinationCountry)
            .fobValue(newFobValue != null ? newFobValue : this.fobValue)
            .status(this.status)
            .createdAt(this.createdAt)
            .active(this.active)
            .supplier(this.supplier)
            .items(this.items)
            .build();
    }

}