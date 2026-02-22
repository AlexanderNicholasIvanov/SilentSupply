package com.silentsupply.product.dto;

import com.silentsupply.currency.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a product listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    /** Product name. */
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    /** Product description. */
    private String description;

    /** Product category. */
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    /** Stock Keeping Unit. */
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    /** Unit of measure. */
    @NotBlank(message = "Unit of measure is required")
    @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
    private String unitOfMeasure;

    /** Base price per unit. */
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    /** Available stock quantity. */
    @NotNull(message = "Available quantity is required")
    @PositiveOrZero(message = "Available quantity must not be negative")
    private Integer availableQuantity;

    /** Currency for prices (defaults to USD if not specified). */
    private Currency currency;
}
