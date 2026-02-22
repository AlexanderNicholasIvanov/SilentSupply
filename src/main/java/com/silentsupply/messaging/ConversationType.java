package com.silentsupply.messaging;

/**
 * Types of conversations in the messaging system.
 */
public enum ConversationType {

    /** Direct company-to-company conversation. */
    DIRECT,

    /** Conversation scoped to a specific RFQ. */
    RFQ,

    /** Conversation scoped to a specific order. */
    ORDER
}
