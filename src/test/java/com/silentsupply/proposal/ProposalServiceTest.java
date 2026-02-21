package com.silentsupply.proposal;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqService;
import com.silentsupply.rfq.RfqStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProposalService}.
 */
@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private RfqRepository rfqRepository;
    @Mock
    private RfqService rfqService;
    @Mock
    private ProposalMapper proposalMapper;

    @InjectMocks
    private ProposalService proposalService;

    private Company buyer;
    private Company supplier;
    private Product product;
    private Rfq rfq;

    @BeforeEach
    void setUp() {
        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);
        buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(2L);
        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .status(RfqStatus.SUBMITTED).currentRound(0).maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        rfq.setId(100L);
    }

    @Test
    void createBuyerProposal_withValidRequest_createsProposal() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        ProposalResponse expectedResponse = ProposalResponse.builder()
                .id(200L).rfqId(100L).proposerType(ProposerType.BUYER)
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14)
                .status(ProposalStatus.PENDING).roundNumber(1).build();

        when(rfqService.findRfqOrThrow(100L)).thenReturn(rfq);
        when(proposalRepository.save(any(Proposal.class))).thenAnswer(inv -> {
            Proposal p = inv.getArgument(0);
            p.setId(200L);
            return p;
        });
        when(proposalMapper.toResponse(any(Proposal.class))).thenReturn(expectedResponse);

        ProposalResponse result = proposalService.createBuyerProposal(100L, 2L, request);

        assertThat(result.getRoundNumber()).isEqualTo(1);
        assertThat(result.getProposerType()).isEqualTo(ProposerType.BUYER);
        assertThat(rfq.getCurrentRound()).isEqualTo(1);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.UNDER_REVIEW);
        verify(rfqRepository).save(rfq);
    }

    @Test
    void createBuyerProposal_byNonOwner_throwsBusinessRuleException() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        when(rfqService.findRfqOrThrow(100L)).thenReturn(rfq);

        assertThatThrownBy(() -> proposalService.createBuyerProposal(100L, 999L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only the RFQ owner");
    }

    @Test
    void createBuyerProposal_onAcceptedRfq_throwsBusinessRuleException() {
        rfq.setStatus(RfqStatus.ACCEPTED);
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        when(rfqService.findRfqOrThrow(100L)).thenReturn(rfq);

        assertThatThrownBy(() -> proposalService.createBuyerProposal(100L, 2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not in a status");
    }

    @Test
    void createBuyerProposal_atMaxRounds_throwsBusinessRuleException() {
        rfq.setCurrentRound(3);
        rfq.setMaxRounds(3);
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        when(rfqService.findRfqOrThrow(100L)).thenReturn(rfq);

        assertThatThrownBy(() -> proposalService.createBuyerProposal(100L, 2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maximum negotiation rounds");
    }
}
