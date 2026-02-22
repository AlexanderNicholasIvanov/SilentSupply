package com.silentsupply.analytics.dto;

import com.silentsupply.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for the buyer analytics dashboard.
 * Aggregates order, spend, and RFQ metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerDashboardResponse {

    /** Total number of orders placed. */
    private long totalOrdersPlaced;

    /** Total spend across all orders. */
    private BigDecimal totalSpend;

    /** Average order value. */
    private BigDecimal averageOrderValue;

    /** Percentage of RFQs that resulted in ACCEPTED status. */
    private BigDecimal rfqSuccessRate;

    /** Total number of RFQs submitted. */
    private long totalRfqs;

    /** Order counts grouped by status. */
    private Map<OrderStatus, Long> ordersByStatus;
}
