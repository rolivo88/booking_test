package com.company.booking.infrastructure.repository;

import com.company.booking.infrastructure.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository
        extends JpaRepository<SupplierEntity, Long> {

    Optional<SupplierEntity> findByTaxId(String taxId);

    boolean existsByTaxId(String taxId);
}
