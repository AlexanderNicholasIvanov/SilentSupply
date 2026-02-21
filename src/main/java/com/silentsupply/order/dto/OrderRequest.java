package com.silentsupply.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for placing a new catalog order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    /** The product to order. */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** Quantity to order. */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
