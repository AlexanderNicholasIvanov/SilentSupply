package com.silentsupply.product;

/**
 * Status of a product listing in the catalog.
 */
public enum ProductStatus {

    /** Product is available for purchase. */
    ACTIVE,

    /** Product is temporarily unavailable. */
    OUT_OF_STOCK,

    /** Product has been permanently removed from the catalog. */
    DISCONTINUED
}
