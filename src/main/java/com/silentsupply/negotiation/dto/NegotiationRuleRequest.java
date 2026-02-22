package com.silentsupply.negotiation.dto;

import com.silentsupply.currency.Currency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating negotiation rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationRuleRequest {

    /** Product ID to apply rules to. */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** Absolute minimum price. */
    @NotNull(message = "Price floor is required")
    @Positive(message = "Price floor must be positive")
    private BigDecimal priceFloor;

    /** Price at or above which auto-accept triggers. */
    @NotNull(message = "Auto-accept threshold is required")
    @Positive(message = "Auto-accept threshold must be positive")
    private BigDecimal autoAcceptThreshold;

    /** Maximum delivery days. */
    @NotNull(message = "Max delivery days is required")
    @Positive(message = "Max delivery days must be positive")
    private Integer maxDeliveryDays;

    /** Maximum negotiation rounds. */
    @NotNull(message = "Max rounds is required")
    @Min(value = 1, message = "Max rounds must be at least 1")
    @Max(value = 10, message = "Max rounds must not exceed 10")
    private Integer maxRounds;

    /** Volume discount percentage (0-100). */
    @NotNull(message = "Volume discount percentage is required")
    @Min(value = 0, message = "Volume discount must be non-negative")
    @Max(value = 100, message = "Volume discount must not exceed 100")
    private BigDecimal volumeDiscountPct;

    /** Minimum quantity for volume discount. */
    @NotNull(message = "Volume threshold is required")
    @PositiveOrZero(message = "Volume threshold must not be negative")
    private Integer volumeThreshold;

    /** Currency for prices (defaults to USD if not specified). */
    private Currency currency;
}
