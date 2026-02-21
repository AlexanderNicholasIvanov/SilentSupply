package com.silentsupply.company;

/**
 * Defines the role a company plays in the SilentSupply marketplace.
 * A company is either a supplier or a buyer, never both.
 */
public enum CompanyRole {

    /** Supplier: lists products, configures negotiation rules, fulfills orders. */
    SUPPLIER,

    /** Buyer: browses products, places catalog orders, submits RFQs. */
    BUYER
}
