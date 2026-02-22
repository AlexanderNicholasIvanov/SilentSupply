package com.silentsupply.analytics;

import com.silentsupply.analytics.dto.BuyerDashboardResponse;
import com.silentsupply.analytics.dto.SupplierDashboardResponse;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.notification.NotificationRepository;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.proposal.ProposalRepository;
import com.silentsupply.rfq.RfqRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AnalyticsController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AnalyticsControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CatalogOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private RfqRepository rfqRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private String supplierToken;
    private String buyerToken;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("AnalyticsSupplier", "analytics-supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("AnalyticsBuyer", "analytics-buyer@example.com", CompanyRole.BUYER);
    }

    @Test
    void supplierDashboard_withOrders_returnsMetrics() {
        Long productId = createProduct("Widget", new BigDecimal("25.00"), 100);
        placeOrder(productId, 4);

        ResponseEntity<SupplierDashboardResponse> response = restTemplate.exchange(
                "/api/analytics/supplier", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                SupplierDashboardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SupplierDashboardResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalProducts()).isEqualTo(1);
        assertThat(body.getTotalOrdersReceived()).isEqualTo(1);
        assertThat(body.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(body.getAverageOrderValue()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void supplierDashboard_asBuyer_returns403() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/analytics/supplier", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void buyerDashboard_withOrders_returnsMetrics() {
        Long productId = createProduct("Gadget", new BigDecimal("10.00"), 50);
        placeOrder(productId, 3);

        ResponseEntity<BuyerDashboardResponse> response = restTemplate.exchange(
                "/api/analytics/buyer", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                BuyerDashboardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BuyerDashboardResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalOrdersPlaced()).isEqualTo(1);
        assertThat(body.getTotalSpend()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void buyerDashboard_asSupplier_returns403() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/analytics/buyer", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void supplierDashboard_withNoData_returnsZeros() {
        ResponseEntity<SupplierDashboardResponse> response = restTemplate.exchange(
                "/api/analytics/supplier", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                SupplierDashboardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SupplierDashboardResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalProducts()).isZero();
        assertThat(body.getTotalOrdersReceived()).isZero();
        assertThat(body.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Long createProduct(String name, BigDecimal price, int quantity) {
        ProductRequest request = ProductRequest.builder()
                .name(name).description("Test").category("Electronics").sku(name + "-SKU")
                .unitOfMeasure("piece").basePrice(price).availableQuantity(quantity)
                .build();
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                ProductResponse.class);
        return response.getBody().getId();
    }

    private void placeOrder(Long productId, int quantity) {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(quantity).build();
        restTemplate.exchange("/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                OrderResponse.class);
    }

    private String registerAndGetToken(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        return response.getBody().getToken();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
