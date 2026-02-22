package com.silentsupply.analytics;

import com.silentsupply.analytics.dto.BuyerDashboardResponse;
import com.silentsupply.analytics.dto.SupplierDashboardResponse;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.order.OrderStatus;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AnalyticsService}.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private CatalogOrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RfqRepository rfqRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getSupplierDashboard_withOrders_returnsAggregatedMetrics() {
        Long supplierId = 1L;

        when(productRepository.findBySupplierId(supplierId))
                .thenReturn(List.of(new Product(), new Product()));
        when(orderRepository.findRevenueStatsForSupplier(supplierId))
                .thenReturn(objectArrayList(5L, new BigDecimal("500.00")));
        when(rfqRepository.countBySupplierId(supplierId)).thenReturn(10L);
        when(rfqRepository.countBySupplierIdAndStatus(supplierId, RfqStatus.ACCEPTED)).thenReturn(7L);

        List<Object[]> revenueByProduct = new ArrayList<>();
        revenueByProduct.add(new Object[]{1L, "Widget", new BigDecimal("300.00"), 3L});
        revenueByProduct.add(new Object[]{2L, "Gadget", new BigDecimal("200.00"), 2L});
        when(orderRepository.findRevenueByProductForSupplier(supplierId))
                .thenReturn(revenueByProduct);

        List<Object[]> statusCounts = new ArrayList<>();
        statusCounts.add(new Object[]{OrderStatus.PLACED, 2L});
        statusCounts.add(new Object[]{OrderStatus.DELIVERED, 3L});
        when(orderRepository.countByStatusForSupplier(supplierId))
                .thenReturn(statusCounts);

        SupplierDashboardResponse result = analyticsService.getSupplierDashboard(supplierId);

        assertThat(result.getTotalProducts()).isEqualTo(2);
        assertThat(result.getTotalOrdersReceived()).isEqualTo(5);
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getAverageOrderValue()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getNegotiationSuccessRate()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(result.getTotalRfqs()).isEqualTo(10);
        assertThat(result.getRevenueByProduct()).hasSize(2);
        assertThat(result.getRevenueByProduct().get(0).getProductName()).isEqualTo("Widget");
        assertThat(result.getOrdersByStatus()).containsEntry(OrderStatus.PLACED, 2L);
        assertThat(result.getOrdersByStatus()).containsEntry(OrderStatus.DELIVERED, 3L);
    }

    @Test
    void getSupplierDashboard_withNoData_returnsZeros() {
        Long supplierId = 99L;

        when(productRepository.findBySupplierId(supplierId)).thenReturn(List.of());
        when(orderRepository.findRevenueStatsForSupplier(supplierId))
                .thenReturn(objectArrayList(0L, BigDecimal.ZERO));
        when(rfqRepository.countBySupplierId(supplierId)).thenReturn(0L);
        when(rfqRepository.countBySupplierIdAndStatus(supplierId, RfqStatus.ACCEPTED)).thenReturn(0L);
        when(orderRepository.findRevenueByProductForSupplier(supplierId)).thenReturn(Collections.emptyList());
        when(orderRepository.countByStatusForSupplier(supplierId)).thenReturn(Collections.emptyList());

        SupplierDashboardResponse result = analyticsService.getSupplierDashboard(supplierId);

        assertThat(result.getTotalProducts()).isZero();
        assertThat(result.getTotalOrdersReceived()).isZero();
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAverageOrderValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getNegotiationSuccessRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getRevenueByProduct()).isEmpty();
        assertThat(result.getOrdersByStatus()).isEmpty();
    }

    @Test
    void getBuyerDashboard_withOrders_returnsAggregatedMetrics() {
        Long buyerId = 2L;

        when(orderRepository.findSpendStatsForBuyer(buyerId))
                .thenReturn(objectArrayList(3L, new BigDecimal("150.00")));
        when(rfqRepository.countByBuyerId(buyerId)).thenReturn(5L);
        when(rfqRepository.countByBuyerIdAndStatus(buyerId, RfqStatus.ACCEPTED)).thenReturn(2L);
        List<Object[]> buyerStatusCounts = new ArrayList<>();
        buyerStatusCounts.add(new Object[]{OrderStatus.PLACED, 3L});
        when(orderRepository.countByStatusForBuyer(buyerId))
                .thenReturn(buyerStatusCounts);

        BuyerDashboardResponse result = analyticsService.getBuyerDashboard(buyerId);

        assertThat(result.getTotalOrdersPlaced()).isEqualTo(3);
        assertThat(result.getTotalSpend()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getAverageOrderValue()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getRfqSuccessRate()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(result.getTotalRfqs()).isEqualTo(5);
        assertThat(result.getOrdersByStatus()).containsEntry(OrderStatus.PLACED, 3L);
    }

    @Test
    void getBuyerDashboard_withNoData_returnsZeros() {
        Long buyerId = 99L;

        when(orderRepository.findSpendStatsForBuyer(buyerId))
                .thenReturn(objectArrayList(0L, BigDecimal.ZERO));
        when(rfqRepository.countByBuyerId(buyerId)).thenReturn(0L);
        when(rfqRepository.countByBuyerIdAndStatus(buyerId, RfqStatus.ACCEPTED)).thenReturn(0L);
        when(orderRepository.countByStatusForBuyer(buyerId)).thenReturn(Collections.emptyList());

        BuyerDashboardResponse result = analyticsService.getBuyerDashboard(buyerId);

        assertThat(result.getTotalOrdersPlaced()).isZero();
        assertThat(result.getTotalSpend()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAverageOrderValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getRfqSuccessRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getOrdersByStatus()).isEmpty();
    }

    /**
     * Helper to create a single-element List of Object[] without Java varargs ambiguity.
     */
    private List<Object[]> objectArrayList(Object... elements) {
        List<Object[]> list = new ArrayList<>();
        list.add(elements);
        return list;
    }
}
