package com.silentsupply.currency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for exchange rate lookup and persistence.
 */
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Finds the most recent exchange rate for a currency pair.
     *
     * @param fromCurrency source currency
     * @param toCurrency   target currency
     * @return the latest rate, if any
     */
    Optional<ExchangeRate> findTopByFromCurrencyAndToCurrencyOrderByEffectiveDateDesc(
            Currency fromCurrency, Currency toCurrency);

    /**
     * Finds all rates involving specific currencies.
     *
     * @param fromCurrency source currency
     * @param toCurrency   target currency
     * @return matching exchange rates
     */
    List<ExchangeRate> findByFromCurrencyAndToCurrency(Currency fromCurrency, Currency toCurrency);
}
