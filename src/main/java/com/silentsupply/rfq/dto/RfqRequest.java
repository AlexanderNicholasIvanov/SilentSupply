package com.silentsupply.rfq.dto;

import com.silentsupply.currency.Currency;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for submitting a new RFQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfqRequest {

    /** The product to request a quote for. */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** Desired quantity. */
    @NotNull(message = "Desired quantity is required")
    @Positive(message = "Desired quantity must be positive")
    private Integer desiredQuantity;

    /** Target price per unit. */
    @NotNull(message = "Target price is required")
    @Positive(message = "Target price must be positive")
    private BigDecimal targetPrice;

    /** Required delivery deadline. */
    @NotNull(message = "Delivery deadline is required")
    @Future(message = "Delivery deadline must be in the future")
    private LocalDate deliveryDeadline;

    /** Optional notes for the supplier. */
    private String notes;

    /** Currency for prices (defaults to USD if not specified). */
    private Currency currency;
}
