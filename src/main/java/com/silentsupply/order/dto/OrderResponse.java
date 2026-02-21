package com.silentsupply.order.dto;

import com.silentsupply.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a catalog order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /** Order ID. */
    private Long id;

    /** Buyer's company ID. */
    private Long buyerId;

    /** Buyer's company name. */
    private String buyerName;

    /** Product ID. */
    private Long productId;

    /** Product name. */
    private String productName;

    /** Supplier's company ID. */
    private Long supplierId;

    /** Supplier's company name. */
    private String supplierName;

    /** Quantity ordered. */
    private int quantity;

    /** Price per unit at time of order. */
    private BigDecimal unitPrice;

    /** Total order value. */
    private BigDecimal totalPrice;

    /** Current order status. */
    private OrderStatus status;

    /** When the order was placed. */
    private LocalDateTime createdAt;
}
