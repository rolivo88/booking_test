package com.company.booking.domain.model.enums;

public enum BookingStatus {

    DRAFT,
    CONFIRMED,
    CANCELLED;

    public boolean canBeModified(){
        return this == DRAFT;
    }

    public boolean canBeCancelled(){
        return this == CONFIRMED;
    }
}