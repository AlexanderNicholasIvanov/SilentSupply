package com.silentsupply.order;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CatalogOrderMapper}.
 */
class CatalogOrderMapperTest {

    private final CatalogOrderMapper mapper = Mappers.getMapper(CatalogOrderMapper.class);

    @Test
    void toResponse_mapsAllNestedFields() {
        Company buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(1L);

        Company supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(2L);

        Product product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.PLACED).build();
        order.setId(100L);

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBuyerId()).isEqualTo(1L);
        assertThat(response.getBuyerName()).isEqualTo("BuyerCo");
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getProductName()).isEqualTo("Widget");
        assertThat(response.getSupplierId()).isEqualTo(2L);
        assertThat(response.getSupplierName()).isEqualTo("SupplierCo");
        assertThat(response.getQuantity()).isEqualTo(5);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PLACED);
    }
}
