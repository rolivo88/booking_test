package com.company.booking.infrastructure.repository;

import com.company.booking.infrastructure.entity.BookingRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BookingRequestRepository
        extends JpaRepository<BookingRequestEntity, Long>,
                JpaSpecificationExecutor<BookingRequestEntity> {

    Optional<BookingRequestEntity> findByBookingCode(String bookingCode);

    boolean existsByBookingCode(String bookingCode);
}
