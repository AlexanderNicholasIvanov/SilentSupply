package com.silentsupply.product.dto;

import com.silentsupply.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Search criteria DTO for filtering products. All fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

    /** Filter by exact category match. */
    private String category;

    /** Filter by name (case-insensitive contains). */
    private String name;

    /** Minimum price filter. */
    private BigDecimal minPrice;

    /** Maximum price filter. */
    private BigDecimal maxPrice;

    /** Filter by product status. */
    private ProductStatus status;
}
