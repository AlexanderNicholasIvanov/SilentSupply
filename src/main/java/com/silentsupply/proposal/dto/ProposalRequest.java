package com.silentsupply.proposal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new proposal within an RFQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalRequest {

    /** Proposed price per unit. */
    @NotNull(message = "Proposed price is required")
    @Positive(message = "Proposed price must be positive")
    private BigDecimal proposedPrice;

    /** Proposed quantity. */
    @NotNull(message = "Proposed quantity is required")
    @Positive(message = "Proposed quantity must be positive")
    private Integer proposedQty;

    /** Proposed delivery time in days. */
    @NotNull(message = "Delivery days is required")
    @Positive(message = "Delivery days must be positive")
    private Integer deliveryDays;
}
