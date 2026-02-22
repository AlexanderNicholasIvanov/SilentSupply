package com.silentsupply.currency;

import com.silentsupply.common.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for currency conversion using the latest available exchange rates.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrencyService {

    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * Converts an amount from one currency to another using the latest exchange rate.
     * Returns the amount unchanged if both currencies are the same.
     *
     * @param amount the amount to convert
     * @param from   source currency
     * @param to     target currency
     * @return the converted amount, rounded to 2 decimal places
     * @throws BusinessRuleException if no exchange rate is found for the pair
     */
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from == to) {
            return amount;
        }

        ExchangeRate rate = exchangeRateRepository
                .findTopByFromCurrencyAndToCurrencyOrderByEffectiveDateDesc(from, to)
                .orElseThrow(() -> new BusinessRuleException(
                        "No exchange rate found for " + from + " to " + to));

        return amount.multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP);
    }
}
