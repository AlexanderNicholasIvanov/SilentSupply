package com.silentsupply.proposal;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProposalMapper}.
 */
class ProposalMapperTest {

    private final ProposalMapper mapper = Mappers.getMapper(ProposalMapper.class);

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
                .status(RfqStatus.SUBMITTED).currentRound(1).maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        rfq.setId(100L);

        Proposal proposal = Proposal.builder()
                .rfq(rfq).proposerType(ProposerType.BUYER)
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14)
                .status(ProposalStatus.PENDING).roundNumber(1).build();
        proposal.setId(200L);

        ProposalResponse response = mapper.toResponse(proposal);

        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getRfqId()).isEqualTo(100L);
        assertThat(response.getProposerType()).isEqualTo(ProposerType.BUYER);
        assertThat(response.getProposedPrice()).isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(response.getProposedQty()).isEqualTo(50);
        assertThat(response.getDeliveryDays()).isEqualTo(14);
        assertThat(response.getStatus()).isEqualTo(ProposalStatus.PENDING);
        assertThat(response.getRoundNumber()).isEqualTo(1);
    }
}
