package com.company.booking.infrastructure.adapter.out;

import com.company.booking.domain.model.Supplier;
import com.company.booking.infrastructure.entity.SupplierEntity;
import com.company.booking.infrastructure.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierAdapter — pruebas unitarias")
class SupplierAdapterTest {

    @Mock
    private SupplierRepository repository;

    @InjectMocks
    private SupplierAdapter adapter;

    private SupplierEntity supplierEntity;

    @BeforeEach
    void setUp() {
        supplierEntity = SupplierEntity.builder()
                .id(1L).name("Proveedor SA").taxId("12345678-9")
                .country("Chile").address("Calle 1").contactEmail("p@test.com")
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("findByTaxId retorna supplier cuando existe")
    void findByTaxId_encontrado() {
        when(repository.findByTaxId("12345678-9")).thenReturn(Optional.of(supplierEntity));

        Optional<Supplier> result = adapter.findByTaxId("12345678-9");

        assertThat(result).isPresent();
        assertThat(result.get().getTaxId()).isEqualTo("12345678-9");
        assertThat(result.get().getName()).isEqualTo("Proveedor SA");
        assertThat(result.get().getCountry()).isEqualTo("Chile");
    }

    @Test
    @DisplayName("findByTaxId retorna Optional vacío cuando no existe")
    void findByTaxId_noEncontrado() {
        when(repository.findByTaxId("NOPE")).thenReturn(Optional.empty());

        assertThat(adapter.findByTaxId("NOPE")).isEmpty();
    }

    @Test
    @DisplayName("existsByTaxId retorna true cuando el taxId existe")
    void existsByTaxId_true() {
        when(repository.existsByTaxId("12345678-9")).thenReturn(true);
        assertThat(adapter.existsByTaxId("12345678-9")).isTrue();
    }

    @Test
    @DisplayName("existsByTaxId retorna false cuando el taxId no existe")
    void existsByTaxId_false() {
        when(repository.existsByTaxId("NOPE")).thenReturn(false);
        assertThat(adapter.existsByTaxId("NOPE")).isFalse();
    }
}
