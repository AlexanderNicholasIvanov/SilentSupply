package com.silentsupply.product;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProductMapper}.
 */
class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toEntity_mapsRequestFields() {
        ProductRequest request = ProductRequest.builder()
                .name("Widget A")
                .description("A fine widget")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .build();

        Product product = mapper.toEntity(request);

        assertThat(product.getId()).isNull();
        assertThat(product.getSupplier()).isNull();
        assertThat(product.getName()).isEqualTo("Widget A");
        assertThat(product.getCategory()).isEqualTo("Electronics");
        assertThat(product.getSku()).isEqualTo("WDG-001");
        assertThat(product.getBasePrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(product.getAvailableQuantity()).isEqualTo(100);
    }

    @Test
    void toResponse_mapsEntityFieldsIncludingSupplier() {
        Company supplier = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("hashed")
                .role(CompanyRole.SUPPLIER)
                .build();
        supplier.setId(1L);

        Product product = Product.builder()
                .supplier(supplier)
                .name("Widget A")
                .description("A fine widget")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        product.setId(10L);

        ProductResponse response = mapper.toResponse(product);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getSupplierId()).isEqualTo(1L);
        assertThat(response.getSupplierName()).isEqualTo("Acme Corp");
        assertThat(response.getName()).isEqualTo("Widget A");
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void updateEntity_updatesExistingProduct() {
        Product existing = Product.builder()
                .name("Old Name")
                .description("Old desc")
                .category("Old Cat")
                .sku("OLD-001")
                .unitOfMeasure("kg")
                .basePrice(new BigDecimal("10.00"))
                .availableQuantity(50)
                .build();
        existing.setId(5L);

        ProductRequest request = ProductRequest.builder()
                .name("New Name")
                .description("New desc")
                .category("New Cat")
                .sku("NEW-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("25.00"))
                .availableQuantity(200)
                .build();

        mapper.updateEntity(request, existing);

        assertThat(existing.getId()).isEqualTo(5L);
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getBasePrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(existing.getAvailableQuantity()).isEqualTo(200);
    }
}
