package com.silentsupply.analytics;

import com.silentsupply.analytics.dto.BuyerDashboardResponse;
import com.silentsupply.analytics.dto.ProductRevenueResponse;
import com.silentsupply.analytics.dto.SupplierDashboardResponse;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.order.OrderStatus;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Service for computing analytics dashboards from existing order, product, and RFQ data.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final int TOP_PRODUCTS_LIMIT = 5;

    private final CatalogOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RfqRepository rfqRepository;

    /**
     * Builds the supplier analytics dashboard.
     *
     * @param supplierId the supplier's company ID
     * @return aggregated supplier metrics
     */
    public SupplierDashboardResponse getSupplierDashboard(Long supplierId) {
        long totalProducts = productRepository.findBySupplierId(supplierId).size();

        Object[] revenueStats = orderRepository.findRevenueStatsForSupplier(supplierId).get(0);
        long totalOrders = ((Number) revenueStats[0]).longValue();
        BigDecimal totalRevenue = toBigDecimal(revenueStats[1]);
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long totalRfqs = rfqRepository.countBySupplierId(supplierId);
        long acceptedRfqs = rfqRepository.countBySupplierIdAndStatus(supplierId, RfqStatus.ACCEPTED);
        BigDecimal successRate = totalRfqs > 0
                ? BigDecimal.valueOf(acceptedRfqs)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalRfqs), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Object[]> revenueByProduct = orderRepository.findRevenueByProductForSupplier(supplierId);
        List<ProductRevenueResponse> topProducts = revenueByProduct.stream()
                .limit(TOP_PRODUCTS_LIMIT)
                .map(row -> ProductRevenueResponse.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .revenue(toBigDecimal(row[2]))
                        .orderCount(((Number) row[3]).longValue())
                        .build())
                .toList();

        Map<OrderStatus, Long> ordersByStatus = buildOrderStatusMap(
                orderRepository.countByStatusForSupplier(supplierId));

        return SupplierDashboardResponse.builder()
                .totalProducts(totalProducts)
                .totalOrdersReceived(totalOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .negotiationSuccessRate(successRate)
                .totalRfqs(totalRfqs)
                .revenueByProduct(topProducts)
                .ordersByStatus(ordersByStatus)
                .build();
    }

    /**
     * Builds the buyer analytics dashboard.
     *
     * @param buyerId the buyer's company ID
     * @return aggregated buyer metrics
     */
    public BuyerDashboardResponse getBuyerDashboard(Long buyerId) {
        Object[] spendStats = orderRepository.findSpendStatsForBuyer(buyerId).get(0);
        long totalOrders = ((Number) spendStats[0]).longValue();
        BigDecimal totalSpend = toBigDecimal(spendStats[1]);
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalSpend.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long totalRfqs = rfqRepository.countByBuyerId(buyerId);
        long acceptedRfqs = rfqRepository.countByBuyerIdAndStatus(buyerId, RfqStatus.ACCEPTED);
        BigDecimal successRate = totalRfqs > 0
                ? BigDecimal.valueOf(acceptedRfqs)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalRfqs), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<OrderStatus, Long> ordersByStatus = buildOrderStatusMap(
                orderRepository.countByStatusForBuyer(buyerId));

        return BuyerDashboardResponse.builder()
                .totalOrdersPlaced(totalOrders)
                .totalSpend(totalSpend)
                .averageOrderValue(averageOrderValue)
                .rfqSuccessRate(successRate)
                .totalRfqs(totalRfqs)
                .ordersByStatus(ordersByStatus)
                .build();
    }

    /**
     * Converts raw [status, count] query results into an OrderStatus map.
     *
     * @param statusCounts raw query results
     * @return map of status to count
     */
    private Map<OrderStatus, Long> buildOrderStatusMap(List<Object[]> statusCounts) {
        Map<OrderStatus, Long> map = new EnumMap<>(OrderStatus.class);
        for (Object[] row : statusCounts) {
            map.put((OrderStatus) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    /**
     * Safely converts a query result value to BigDecimal.
     *
     * @param value the value from a JPQL query result
     * @return the BigDecimal representation
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
