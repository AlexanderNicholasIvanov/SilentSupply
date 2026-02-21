package com.silentsupply.negotiation;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.proposal.Proposal;
import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.proposal.ProposerType;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NegotiationEngine}.
 * Tests all outcomes: auto-accept, auto-counter, auto-reject, max rounds, and volume discounts.
 */
class NegotiationEngineTest {

    private NegotiationEngine engine;
    private NegotiationRule rule;
    private Rfq rfq;
    private Company supplier;
    private Company buyer;
    private Product product;

    @BeforeEach
    void setUp() {
        engine = new NegotiationEngine();

        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);
        buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(2L);
        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(1000)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        rule = NegotiationRule.builder()
                .supplier(supplier).product(product)
                .priceFloor(new BigDecimal("7.00"))
                .autoAcceptThreshold(new BigDecimal("9.50"))
                .maxDeliveryDays(30)
                .maxRounds(3)
                .volumeDiscountPct(new BigDecimal("5.00"))
                .volumeThreshold(100)
                .build();
        rule.setId(50L);

        rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .status(RfqStatus.UNDER_REVIEW).currentRound(1).maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        rfq.setId(100L);
    }

    @Test
    void evaluate_priceAboveThreshold_autoAccepts() {
        Proposal proposal = buildProposal(new BigDecimal("9.50"), 50, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(result.getReasonCode()).isEqualTo("AUTO_ACCEPTED");
        assertThat(result.isCounterGenerated()).isFalse();
    }

    @Test
    void evaluate_priceAboveFloorBelowThreshold_autoCounters() {
        Proposal proposal = buildProposal(new BigDecimal("8.00"), 50, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.COUNTERED);
        assertThat(result.getReasonCode()).isEqualTo("AUTO_COUNTERED");
        assertThat(result.isCounterGenerated()).isTrue();
        assertThat(result.getCounterPrice()).isEqualByComparingTo(new BigDecimal("9.50"));
        assertThat(result.getCounterQty()).isEqualTo(50);
    }

    @Test
    void evaluate_priceBelowFloor_autoRejects() {
        Proposal proposal = buildProposal(new BigDecimal("5.00"), 50, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(result.getReasonCode()).isEqualTo("PRICE_BELOW_FLOOR");
        assertThat(result.isCounterGenerated()).isFalse();
    }

    @Test
    void evaluate_deliveryExceedsMax_autoRejects() {
        Proposal proposal = buildProposal(new BigDecimal("9.00"), 50, 60);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(result.getReasonCode()).isEqualTo("DELIVERY_EXCEEDS_MAX");
    }

    @Test
    void evaluate_maxRoundsExceeded_autoRejects() {
        rfq.setCurrentRound(4);
        Proposal proposal = buildProposal(new BigDecimal("9.00"), 50, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(result.getReasonCode()).isEqualTo("MAX_ROUNDS_EXCEEDED");
    }

    @Test
    void evaluate_withVolumeDiscount_lowersEffectivePrices() {
        Proposal proposal = buildProposal(new BigDecimal("9.03"), 150, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(result.getReasonCode()).isEqualTo("AUTO_ACCEPTED");
    }

    @Test
    void evaluate_withVolumeDiscount_belowDiscountedFloor_rejects() {
        Proposal proposal = buildProposal(new BigDecimal("6.00"), 150, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(result.getReasonCode()).isEqualTo("PRICE_BELOW_FLOOR");
    }

    @Test
    void evaluate_withVolumeDiscount_inNegotiableRange_countersWithDiscountedPrice() {
        Proposal proposal = buildProposal(new BigDecimal("7.50"), 150, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.COUNTERED);
        assertThat(result.isCounterGenerated()).isTrue();
        assertThat(result.getCounterPrice()).isEqualByComparingTo(new BigDecimal("9.03"));
    }

    @Test
    void calculateEffectivePrice_belowThreshold_noDiscount() {
        BigDecimal result = engine.calculateEffectivePrice(new BigDecimal("10.00"), 50, rule);

        assertThat(result).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void calculateEffectivePrice_atThreshold_appliesDiscount() {
        BigDecimal result = engine.calculateEffectivePrice(new BigDecimal("10.00"), 100, rule);

        assertThat(result).isEqualByComparingTo(new BigDecimal("9.50"));
    }

    /**
     * Builds a test proposal with the given parameters.
     *
     * @param price        the proposed price
     * @param qty          the proposed quantity
     * @param deliveryDays the proposed delivery days
     * @return the test proposal
     */
    private Proposal buildProposal(BigDecimal price, int qty, int deliveryDays) {
        Proposal proposal = Proposal.builder()
                .rfq(rfq).proposerType(ProposerType.BUYER)
                .proposedPrice(price).proposedQty(qty).deliveryDays(deliveryDays)
                .status(ProposalStatus.PENDING).roundNumber(1).build();
        proposal.setId(200L);
        return proposal;
    }
}
