package com.silentsupply.analytics.dto;

import com.silentsupply.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for the supplier analytics dashboard.
 * Aggregates order, revenue, and negotiation metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDashboardResponse {

    /** Total number of products listed by the supplier. */
    private long totalProducts;

    /** Total number of orders received. */
    private long totalOrdersReceived;

    /** Total revenue across all orders. */
    private BigDecimal totalRevenue;

    /** Average order value. */
    private BigDecimal averageOrderValue;

    /** Percentage of RFQs that resulted in ACCEPTED status. */
    private BigDecimal negotiationSuccessRate;

    /** Total number of RFQs received. */
    private long totalRfqs;

    /** Top revenue-generating products (up to 5). */
    private List<ProductRevenueResponse> revenueByProduct;

    /** Order counts grouped by status. */
    private Map<OrderStatus, Long> ordersByStatus;
}
