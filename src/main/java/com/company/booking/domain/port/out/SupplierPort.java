package com.company.booking.domain.port.out;

import com.company.booking.domain.model.Supplier;

import java.util.Optional;

/**
 * Puerto de salida: contrato que el dominio necesita
 * para consultar Supplier.
 * La implementación vive en infrastructure.
 */
public interface SupplierPort {

    Optional<Supplier> findByTaxId(String taxId);

    boolean existsByTaxId(String taxId);
}
