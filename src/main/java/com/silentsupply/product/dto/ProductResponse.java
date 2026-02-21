package com.silentsupply.product.dto;

import com.silentsupply.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a product listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    /** Product ID. */
    private Long id;

    /** Supplier's company ID. */
    private Long supplierId;

    /** Supplier's company name. */
    private String supplierName;

    /** Product name. */
    private String name;

    /** Product description. */
    private String description;

    /** Product category. */
    private String category;

    /** Stock Keeping Unit. */
    private String sku;

    /** Unit of measure. */
    private String unitOfMeasure;

    /** Base price per unit. */
    private BigDecimal basePrice;

    /** Available stock quantity. */
    private int availableQuantity;

    /** Current listing status. */
    private ProductStatus status;

    /** When the product was created. */
    private LocalDateTime createdAt;
}
