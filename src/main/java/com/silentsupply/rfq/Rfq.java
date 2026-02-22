package com.silentsupply.rfq;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.company.Company;
import com.silentsupply.currency.Currency;
import com.silentsupply.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a Request for Quote submitted by a buyer for bulk or custom orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rfqs")
public class Rfq extends BaseEntity {

    /** The buyer who submitted the RFQ. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Company buyer;

    /** The product being requested. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** The supplier who owns the product. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    /** Desired quantity. */
    @Column(name = "desired_quantity", nullable = false)
    private int desiredQuantity;

    /** Target price per unit the buyer hopes to achieve. */
    @Column(name = "target_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetPrice;

    /** Required delivery deadline. */
    @Column(name = "delivery_deadline", nullable = false)
    private LocalDate deliveryDeadline;

    /** Optional notes from the buyer. */
    private String notes;

    /** Current RFQ status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RfqStatus status = RfqStatus.SUBMITTED;

    /** Current negotiation round (0 = no proposals yet). */
    @Column(name = "current_round", nullable = false)
    @Builder.Default
    private int currentRound = 0;

    /** Maximum allowed negotiation rounds. */
    @Column(name = "max_rounds", nullable = false)
    @Builder.Default
    private int maxRounds = 3;

    /** When this RFQ expires. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Currency for prices. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    @Builder.Default
    private Currency currency = Currency.USD;
}
