package com.silentsupply.rfq;

/**
 * Status flow for RFQs.
 * SUBMITTED -> UNDER_REVIEW -> COUNTERED/ACCEPTED/REJECTED, or -> EXPIRED.
 */
public enum RfqStatus {

    /** RFQ has been submitted by the buyer. */
    SUBMITTED,

    /** RFQ is being evaluated by the negotiation engine. */
    UNDER_REVIEW,

    /** A counter-proposal has been generated. */
    COUNTERED,

    /** RFQ has been accepted — terms agreed. */
    ACCEPTED,

    /** RFQ has been rejected — no agreement possible. */
    REJECTED,

    /** RFQ has expired without resolution. */
    EXPIRED
}
