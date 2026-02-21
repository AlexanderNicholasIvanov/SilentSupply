package com.silentsupply.negotiation;

import com.silentsupply.proposal.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Encapsulates the outcome of the negotiation engine's evaluation of a buyer proposal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationResult {

    /** The outcome for the buyer's proposal. */
    private ProposalStatus buyerProposalStatus;

    /** Reason code explaining the outcome. */
    private String reasonCode;

    /** Whether a counter-proposal should be generated. */
    private boolean counterGenerated;

    /** Counter-proposed price (if counter generated). */
    private BigDecimal counterPrice;

    /** Counter-proposed quantity (if counter generated). */
    private int counterQty;

    /** Counter-proposed delivery days (if counter generated). */
    private int counterDeliveryDays;
}
