package com.company.booking.infrastructure.adapter.out;

import com.company.booking.domain.model.Supplier;
import com.company.booking.domain.port.out.SupplierPort;
import com.company.booking.infrastructure.mapper.BookingMapper;
import com.company.booking.infrastructure.repository.SupplierRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de salida: implementa SupplierPort usando Spring Data JPA.
 */
@Component
public class SupplierAdapter implements SupplierPort {

    private final SupplierRepository repository;

    public SupplierAdapter(SupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Supplier> findByTaxId(String taxId) {
        return repository.findByTaxId(taxId)
                .map(BookingMapper::toDomain);
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return repository.existsByTaxId(taxId);
    }
}
