package com.silentsupply.notification;

/**
 * Types of entities that a notification can reference.
 */
public enum NotificationReferenceType {

    /** References a catalog order. */
    ORDER,

    /** References an RFQ. */
    RFQ,

    /** References a proposal. */
    PROPOSAL
}
