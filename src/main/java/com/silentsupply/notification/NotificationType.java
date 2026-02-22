package com.silentsupply.notification;

/**
 * Types of notifications that can be sent to companies.
 */
public enum NotificationType {

    /** An order's status has changed. */
    ORDER_STATUS_CHANGED,

    /** A new RFQ has been submitted to a supplier. */
    RFQ_SUBMITTED,

    /** A new proposal has been received for an RFQ. */
    PROPOSAL_RECEIVED,

    /** A negotiation has been resolved (accepted or rejected). */
    NEGOTIATION_RESOLVED
}
