package com.silentsupply.negotiation;

import com.silentsupply.currency.CurrencyService;
import com.silentsupply.proposal.Proposal;
import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.rfq.Rfq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core negotiation engine implementing deterministic, rule-based evaluation of proposals.
 *
 * <p>Decision logic:
 * <ul>
 *   <li>If max rounds exceeded: AUTO-REJECT with MAX_ROUNDS_EXCEEDED</li>
 *   <li>If delivery exceeds max: AUTO-REJECT with DELIVERY_EXCEEDS_MAX</li>
 *   <li>If proposed price &lt; price floor: AUTO-REJECT with PRICE_BELOW_FLOOR</li>
 *   <li>If proposed price &gt;= auto-accept threshold AND delivery within limits: AUTO-ACCEPT</li>
 *   <li>If proposed price &gt;= price floor AND delivery within limits: AUTO-COUNTER with best terms</li>
 * </ul>
 *
 * <p>Volume discounts are applied when the proposed quantity meets or exceeds the volume threshold,
 * reducing the effective price floor and auto-accept threshold accordingly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NegotiationEngine {

    private final CurrencyService currencyService;

    /**
     * Evaluates a buyer proposal against supplier-defined negotiation rules.
     *
     * @param proposal the buyer's proposal
     * @param rfq      the associated RFQ
     * @param rule     the supplier's negotiation rules for this product
     * @return the negotiation result describing the outcome
     */
    public NegotiationResult evaluate(Proposal proposal, Rfq rfq, NegotiationRule rule) {
        log.debug("Evaluating proposal {} for RFQ {} against rule {}",
                proposal.getId(), rfq.getId(), rule.getId());

        if (rfq.getCurrentRound() > rule.getMaxRounds()) {
            return NegotiationResult.builder()
                    .buyerProposalStatus(ProposalStatus.REJECTED)
                    .reasonCode("MAX_ROUNDS_EXCEEDED")
                    .counterGenerated(false)
                    .build();
        }

        BigDecimal effectiveFloor = calculateEffectivePrice(
                rule.getPriceFloor(), proposal.getProposedQty(), rule);
        BigDecimal effectiveThreshold = calculateEffectivePrice(
                rule.getAutoAcceptThreshold(), proposal.getProposedQty(), rule);

        // Convert proposal price to rule's currency for comparison
        BigDecimal proposalPriceInRuleCurrency = currencyService.convert(
                proposal.getProposedPrice(), proposal.getCurrency(), rule.getCurrency());

        boolean priceAcceptable = proposalPriceInRuleCurrency.compareTo(effectiveThreshold) >= 0;
        boolean priceNegotiable = proposalPriceInRuleCurrency.compareTo(effectiveFloor) >= 0;
        boolean deliveryAcceptable = proposal.getDeliveryDays() <= rule.getMaxDeliveryDays();

        if (!deliveryAcceptable) {
            return NegotiationResult.builder()
                    .buyerProposalStatus(ProposalStatus.REJECTED)
                    .reasonCode("DELIVERY_EXCEEDS_MAX")
                    .counterGenerated(false)
                    .build();
        }

        if (!priceNegotiable) {
            return NegotiationResult.builder()
                    .buyerProposalStatus(ProposalStatus.REJECTED)
                    .reasonCode("PRICE_BELOW_FLOOR")
                    .counterGenerated(false)
                    .build();
        }

        if (priceAcceptable) {
            return NegotiationResult.builder()
                    .buyerProposalStatus(ProposalStatus.ACCEPTED)
                    .reasonCode("AUTO_ACCEPTED")
                    .counterGenerated(false)
                    .build();
        }

        // Convert counter price back to proposal's currency
        BigDecimal counterPriceInProposalCurrency = currencyService.convert(
                effectiveThreshold, rule.getCurrency(), proposal.getCurrency());
        return NegotiationResult.builder()
                .buyerProposalStatus(ProposalStatus.COUNTERED)
                .reasonCode("AUTO_COUNTERED")
                .counterGenerated(true)
                .counterPrice(counterPriceInProposalCurrency)
                .counterQty(proposal.getProposedQty())
                .counterDeliveryDays(Math.min(proposal.getDeliveryDays(), rule.getMaxDeliveryDays()))
                .build();
    }

    /**
     * Calculates the effective price after applying volume discounts.
     * If the proposed quantity meets or exceeds the volume threshold,
     * the price is reduced by the volume discount percentage.
     *
     * @param basePrice   the original price
     * @param proposedQty the proposed quantity
     * @param rule        the negotiation rule with discount parameters
     * @return the effective price
     */
    BigDecimal calculateEffectivePrice(BigDecimal basePrice, int proposedQty, NegotiationRule rule) {
        if (rule.getVolumeThreshold() > 0
                && proposedQty >= rule.getVolumeThreshold()
                && rule.getVolumeDiscountPct().compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    rule.getVolumeDiscountPct().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            return basePrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return basePrice;
    }
}
