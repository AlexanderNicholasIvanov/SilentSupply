package com.silentsupply.negotiation;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.company.Company;
import com.silentsupply.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Supplier-defined negotiation rules for a specific product.
 * The negotiation engine uses these to auto-accept, counter, or reject proposals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "negotiation_rules")
public class NegotiationRule extends BaseEntity {

    /** The supplier who owns these rules. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    /** The product these rules apply to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Absolute minimum price the supplier will accept. */
    @Column(name = "price_floor", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceFloor;

    /** Price at or above which the system auto-accepts without negotiation. */
    @Column(name = "auto_accept_threshold", nullable = false, precision = 15, scale = 2)
    private BigDecimal autoAcceptThreshold;

    /** Maximum delivery window in days the supplier can fulfill. */
    @Column(name = "max_delivery_days", nullable = false)
    private int maxDeliveryDays;

    /** Maximum negotiation rounds before auto-expiring the RFQ. */
    @Column(name = "max_rounds", nullable = false)
    @Builder.Default
    private int maxRounds = 3;

    /** Volume discount percentage applied when order exceeds the volume threshold. */
    @Column(name = "volume_discount_pct", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal volumeDiscountPct = BigDecimal.ZERO;

    /** Minimum quantity required to qualify for the volume discount. */
    @Column(name = "volume_threshold", nullable = false)
    @Builder.Default
    private int volumeThreshold = 0;
}
