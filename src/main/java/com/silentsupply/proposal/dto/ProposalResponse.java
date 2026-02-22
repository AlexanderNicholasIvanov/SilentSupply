package com.silentsupply.proposal.dto;

import com.silentsupply.currency.Currency;
import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.proposal.ProposerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a proposal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponse {

    /** Proposal ID. */
    private Long id;

    /** RFQ ID this proposal belongs to. */
    private Long rfqId;

    /** Who created this proposal. */
    private ProposerType proposerType;

    /** Proposed price per unit. */
    private BigDecimal proposedPrice;

    /** Proposed quantity. */
    private int proposedQty;

    /** Proposed delivery time in days. */
    private int deliveryDays;

    /** Current status. */
    private ProposalStatus status;

    /** Negotiation round number. */
    private int roundNumber;

    /** Reason code for rejection or counter. */
    private String reasonCode;

    /** Currency for prices. */
    private Currency currency;

    /** When the proposal was created. */
    private LocalDateTime createdAt;
}
