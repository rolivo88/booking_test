package com.company.booking.domain.exception;

/**
 * Se lanza cuando no se encuentra un Supplier por el criterio indicado.
 */
public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(String taxId) {
        super("Proveedor no encontrado con taxId: " + taxId);
    }
}
