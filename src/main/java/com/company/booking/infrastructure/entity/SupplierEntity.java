package com.company.booking.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "supplier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String taxId;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 255)
    private String address;

    @Column(length = 150)
    private String contactEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
        mappedBy = "supplier",
        fetch = FetchType.LAZY
    )
    private List<BookingRequestEntity> bookings;


    @PrePersist
    private void prePersist(){
        createdAt = LocalDateTime.now();
    }
}