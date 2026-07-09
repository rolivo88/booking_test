package com.company.booking.infrastructure.repository;

import com.company.booking.domain.model.enums.BookingStatus;
import com.company.booking.domain.model.enums.FreightMode;
import com.company.booking.infrastructure.entity.BookingRequestEntity;
import com.company.booking.infrastructure.entity.SupplierEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications para filtros dinámicos sobre BookingRequestEntity.
 * Cada método devuelve un Specification componible con and().
 */
public class BookingSpecification {

    private BookingSpecification() {}

    /**
     * Construye un Specification combinando todos los filtros opcionales.
     * Los parámetros null se ignoran (no generan condición SQL).
     */
    public static Specification<BookingRequestEntity> withFilters(
            String taxId,
            BookingStatus status,
            FreightMode freightMode,
            LocalDate dateFrom,
            LocalDate dateTo,
            String bookingCode
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // JOIN con supplier para filtrar por taxId
            if (taxId != null && !taxId.isBlank()) {
                Join<BookingRequestEntity, SupplierEntity> supplier =
                        root.join("supplier", JoinType.INNER);
                predicates.add(cb.equal(supplier.get("taxId"), taxId));
            }

            // Filtro por status (enum almacenado como String en BD)
            if (status != null) {
                predicates.add(cb.equal(
                    root.get("status"),
                    com.company.booking.infrastructure.entity.BookingStatus
                            .valueOf(status.name())
                ));
            }

            // Filtro por freightMode (almacenado como String)
            if (freightMode != null) {
                predicates.add(cb.equal(
                    root.get("freightMode"),
                    freightMode.name()
                ));
            }

            // Filtro por issueDate >= dateFrom
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("issueDate"), dateFrom
                ));
            }

            // Filtro por issueDate <= dateTo
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("issueDate"), dateTo
                ));
            }

            // Búsqueda exacta por bookingCode
            if (bookingCode != null && !bookingCode.isBlank()) {
                predicates.add(cb.equal(root.get("bookingCode"), bookingCode));
            }

            // Solo registros activos
            predicates.add(cb.isTrue(root.get("active")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
