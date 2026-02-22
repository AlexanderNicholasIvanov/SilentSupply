package com.silentsupply.currency.dto;

import com.silentsupply.currency.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating an exchange rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateRequest {

    /** Source currency. */
    @NotNull(message = "Source currency is required")
    private Currency fromCurrency;

    /** Target currency. */
    @NotNull(message = "Target currency is required")
    private Currency toCurrency;

    /** Conversion rate. */
    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be positive")
    private BigDecimal rate;

    /** Effective date for this rate. */
    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;
}
