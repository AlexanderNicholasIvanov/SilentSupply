package com.silentsupply.currency;

import com.silentsupply.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CurrencyService}.
 */
@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void convert_sameCurrency_returnsOriginalAmount() {
        BigDecimal result = currencyService.convert(new BigDecimal("100.00"), Currency.USD, Currency.USD);

        assertThat(result).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void convert_differentCurrency_appliesRate() {
        ExchangeRate rate = ExchangeRate.builder()
                .fromCurrency(Currency.USD).toCurrency(Currency.EUR)
                .rate(new BigDecimal("0.92000000"))
                .effectiveDate(LocalDate.of(2026, 1, 1))
                .build();

        when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByEffectiveDateDesc(
                Currency.USD, Currency.EUR)).thenReturn(Optional.of(rate));

        BigDecimal result = currencyService.convert(new BigDecimal("100.00"), Currency.USD, Currency.EUR);

        assertThat(result).isEqualByComparingTo(new BigDecimal("92.00"));
    }

    @Test
    void convert_noRateFound_throwsBusinessRuleException() {
        when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByEffectiveDateDesc(
                Currency.USD, Currency.JPY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.convert(new BigDecimal("100.00"), Currency.USD, Currency.JPY))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No exchange rate found");
    }

    @Test
    void convert_roundsToTwoDecimalPlaces() {
        ExchangeRate rate = ExchangeRate.builder()
                .fromCurrency(Currency.USD).toCurrency(Currency.GBP)
                .rate(new BigDecimal("0.79123456"))
                .effectiveDate(LocalDate.of(2026, 1, 1))
                .build();

        when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByEffectiveDateDesc(
                Currency.USD, Currency.GBP)).thenReturn(Optional.of(rate));

        BigDecimal result = currencyService.convert(new BigDecimal("33.33"), Currency.USD, Currency.GBP);

        assertThat(result.scale()).isEqualTo(2);
    }
}
