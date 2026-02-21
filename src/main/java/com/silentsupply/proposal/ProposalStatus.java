package com.silentsupply.proposal;

/**
 * Status of a proposal within an RFQ negotiation.
 */
public enum ProposalStatus {

    /** Proposal is awaiting evaluation. */
    PENDING,

    /** Proposal has been accepted. */
    ACCEPTED,

    /** Proposal has been rejected. */
    REJECTED,

    /** Proposal was countered with a new offer. */
    COUNTERED,

    /** Proposal expired without response. */
    EXPIRED
}
