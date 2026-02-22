package com.silentsupply.currency;

import com.silentsupply.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Stores exchange rates between currency pairs.
 * Multiple rates can exist per pair, keyed by effective date.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "exchange_rates")
public class ExchangeRate extends BaseEntity {

    /** Source currency. */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_currency", nullable = false, length = 3)
    private Currency fromCurrency;

    /** Target currency. */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_currency", nullable = false, length = 3)
    private Currency toCurrency;

    /** Conversion rate (multiply source amount by this to get target amount). */
    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;

    /** Date this rate becomes effective. */
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
}
