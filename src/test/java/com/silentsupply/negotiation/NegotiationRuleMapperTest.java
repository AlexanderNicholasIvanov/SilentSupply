package com.silentsupply.negotiation;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NegotiationRuleMapper}.
 */
class NegotiationRuleMapperTest {

    private final NegotiationRuleMapper mapper = Mappers.getMapper(NegotiationRuleMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        Company supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);

        Product product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        NegotiationRule rule = NegotiationRule.builder()
                .supplier(supplier).product(product)
                .priceFloor(new BigDecimal("7.00")).autoAcceptThreshold(new BigDecimal("9.50"))
                .maxDeliveryDays(30).maxRounds(3)
                .volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();
        rule.setId(50L);

        NegotiationRuleResponse response = mapper.toResponse(rule);

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getSupplierId()).isEqualTo(1L);
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getProductName()).isEqualTo("Widget");
        assertThat(response.getPriceFloor()).isEqualByComparingTo(new BigDecimal("7.00"));
        assertThat(response.getAutoAcceptThreshold()).isEqualByComparingTo(new BigDecimal("9.50"));
        assertThat(response.getMaxDeliveryDays()).isEqualTo(30);
        assertThat(response.getMaxRounds()).isEqualTo(3);
        assertThat(response.getVolumeDiscountPct()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(response.getVolumeThreshold()).isEqualTo(100);
    }
}
