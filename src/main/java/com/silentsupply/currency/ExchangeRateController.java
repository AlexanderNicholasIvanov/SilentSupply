package com.silentsupply.currency;

import com.silentsupply.currency.dto.ExchangeRateRequest;
import com.silentsupply.currency.dto.ExchangeRateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing exchange rates.
 */
@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateMapper exchangeRateMapper;

    /**
     * Lists exchange rates, optionally filtered by currency pair.
     *
     * @param from optional source currency filter
     * @param to   optional target currency filter
     * @return list of exchange rates
     */
    @GetMapping
    public ResponseEntity<List<ExchangeRateResponse>> list(
            @RequestParam(required = false) Currency from,
            @RequestParam(required = false) Currency to) {

        List<ExchangeRate> rates;
        if (from != null && to != null) {
            rates = exchangeRateRepository.findByFromCurrencyAndToCurrency(from, to);
        } else {
            rates = exchangeRateRepository.findAll();
        }

        List<ExchangeRateResponse> responses = rates.stream()
                .map(exchangeRateMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Creates or updates an exchange rate.
     *
     * @param request the exchange rate details
     * @return the saved exchange rate
     */
    @PutMapping
    public ResponseEntity<ExchangeRateResponse> createOrUpdate(@Valid @RequestBody ExchangeRateRequest request) {
        ExchangeRate rate = ExchangeRate.builder()
                .fromCurrency(request.getFromCurrency())
                .toCurrency(request.getToCurrency())
                .rate(request.getRate())
                .effectiveDate(request.getEffectiveDate())
                .build();

        ExchangeRate saved = exchangeRateRepository.save(rate);
        return ResponseEntity.status(HttpStatus.OK).body(exchangeRateMapper.toResponse(saved));
    }
}
