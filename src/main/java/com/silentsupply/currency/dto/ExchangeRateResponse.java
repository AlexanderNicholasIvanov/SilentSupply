package com.silentsupply.currency.dto;

import com.silentsupply.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing an exchange rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponse {

    /** Exchange rate ID. */
    private Long id;

    /** Source currency. */
    private Currency fromCurrency;

    /** Target currency. */
    private Currency toCurrency;

    /** Conversion rate. */
    private BigDecimal rate;

    /** Effective date. */
    private LocalDate effectiveDate;

    /** When the rate was created. */
    private LocalDateTime createdAt;
}
