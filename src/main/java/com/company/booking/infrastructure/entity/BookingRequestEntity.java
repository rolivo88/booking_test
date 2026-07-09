package com.company.booking.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA para booking_request.
 * Solo existe en infrastructure — el dominio no la conoce.
 * incotermCode y freightMode se almacenan como String
 * para desacoplar el esquema de los enums de dominio.
 */
@Entity
@Table(name = "booking_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String bookingCode;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 20)
    private String incotermCode;

    @Column(nullable = false, length = 20)
    private String freightMode;

    @Column(nullable = false, length = 100)
    private String originCountry;

    @Column(nullable = false, length = 100)
    private String destinationCountry;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fobValue;

    /**
     * BookingStatus es el enum propio de infrastructure,
     * sin lógica de negocio, solo para JPA.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;

    @OneToMany(
        mappedBy = "bookingRequest",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<BookingItemEntity> items = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = BookingStatus.DRAFT;
        }
        if (active == null) {
            active = true;
        }
    }
}
