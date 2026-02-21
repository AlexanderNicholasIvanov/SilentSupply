package com.silentsupply.order;

/**
 * Status flow for catalog orders.
 * Valid transitions: PLACED -> CONFIRMED -> SHIPPED -> DELIVERED, or any -> CANCELLED.
 */
public enum OrderStatus {

    /** Order has been placed by the buyer. */
    PLACED,

    /** Order confirmed by the supplier. */
    CONFIRMED,

    /** Order has been shipped. */
    SHIPPED,

    /** Order delivered to the buyer. */
    DELIVERED,

    /** Order has been cancelled. */
    CANCELLED
}
