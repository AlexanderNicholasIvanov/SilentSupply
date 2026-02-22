package com.silentsupply.product;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.company.Company;
import com.silentsupply.currency.Currency;
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

/**
 * Represents a product listed by a supplier in the SilentSupply catalog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    /** The supplier who owns this product listing. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    /** Product name. */
    @Column(nullable = false)
    private String name;

    /** Detailed product description. */
    private String description;

    /** Product category for filtering. */
    @Column(nullable = false)
    private String category;

    /** Stock Keeping Unit — unique per supplier. */
    @Column(nullable = false)
    private String sku;

    /** Unit of measure (e.g., "kg", "piece", "liter"). */
    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;

    /** Base price per unit. */
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    /** Available stock quantity. */
    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    /** Current listing status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    /** Currency for prices. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    @Builder.Default
    private Currency currency = Currency.USD;
}
