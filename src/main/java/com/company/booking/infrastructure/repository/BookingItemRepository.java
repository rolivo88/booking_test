package com.company.booking.infrastructure.repository;

import com.company.booking.infrastructure.entity.BookingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface BookingItemRepository
    extends JpaRepository<BookingItemEntity, Long> {

    List<BookingItemEntity> findByBookingRequestId(
        Long bookingRequestId
    );

}