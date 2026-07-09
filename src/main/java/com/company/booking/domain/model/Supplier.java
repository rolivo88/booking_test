package com.company.booking.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;


@Value
@Builder
public class Supplier {

    Long id;

    String name;

    String taxId;

    String country;

    String address;

    String contactEmail;

    LocalDateTime createdAt;

}