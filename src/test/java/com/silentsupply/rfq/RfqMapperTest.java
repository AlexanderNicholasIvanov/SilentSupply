package com.silentsupply.rfq;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.rfq.dto.RfqResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RfqMapper}.
 */
class RfqMapperTest {

    private final RfqMapper mapper = Mappers.getMapper(RfqMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        Company buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(1L);

        Company supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(2L);

        Product product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        Rfq rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .notes("Urgent").status(RfqStatus.SUBMITTED)
                .currentRound(0).maxRounds(3)
                .expiresAt(LocalDateTime.of(2026, 3, 21, 12, 0))
                .build();
        rfq.setId(100L);

        RfqResponse response = mapper.toResponse(rfq);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBuyerId()).isEqualTo(1L);
        assertThat(response.getBuyerName()).isEqualTo("BuyerCo");
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getProductName()).isEqualTo("Widget");
        assertThat(response.getSupplierId()).isEqualTo(2L);
        assertThat(response.getDesiredQuantity()).isEqualTo(50);
        assertThat(response.getTargetPrice()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(response.getStatus()).isEqualTo(RfqStatus.SUBMITTED);
    }
}
