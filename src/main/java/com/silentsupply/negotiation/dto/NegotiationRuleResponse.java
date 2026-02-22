package com.silentsupply.negotiation.dto;

import com.silentsupply.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a negotiation rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationRuleResponse {

    /** Rule ID. */
    private Long id;

    /** Supplier's company ID. */
    private Long supplierId;

    /** Product ID. */
    private Long productId;

    /** Product name. */
    private String productName;

    /** Absolute minimum price. */
    private BigDecimal priceFloor;

    /** Auto-accept price threshold. */
    private BigDecimal autoAcceptThreshold;

    /** Maximum delivery days. */
    private int maxDeliveryDays;

    /** Maximum negotiation rounds. */
    private int maxRounds;

    /** Volume discount percentage. */
    private BigDecimal volumeDiscountPct;

    /** Minimum quantity for volume discount. */
    private int volumeThreshold;

    /** Currency for prices. */
    private Currency currency;

    /** When the rule was created. */
    private LocalDateTime createdAt;
}
