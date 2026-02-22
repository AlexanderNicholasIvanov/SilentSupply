package com.silentsupply.order;

import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.notification.NotificationRepository;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.order.dto.OrderStatusUpdate;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
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
 * Integration tests for {@link CatalogOrderController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CatalogOrderControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CatalogOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private String supplierToken;
    private String buyerToken;
    private Long productId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("BuyerCo", "buyer@example.com", CompanyRole.BUYER);

        ProductRequest productRequest = ProductRequest.builder()
                .name("Widget").description("Test").category("Electronics").sku("W-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .build();
        ResponseEntity<ProductResponse> productResponse = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productRequest, authHeaders(supplierToken)),
                ProductResponse.class);
        productId = productResponse.getBody().getId();
    }

    @Test
    void placeOrder_asBuyer_returns201() {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(5).build();

        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQuantity()).isEqualTo(5);
        assertThat(response.getBody().getUnitPrice()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(response.getBody().getTotalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.getBody().getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    void placeOrder_asSupplier_returns403() {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(5).build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void placeOrder_withInsufficientStock_returns400() {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(200).build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateStatus_toConfirmed_returns200() {
        OrderRequest orderReq = OrderRequest.builder().productId(productId).quantity(5).build();
        ResponseEntity<OrderResponse> created = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(orderReq, authHeaders(buyerToken)),
                OrderResponse.class);
        Long orderId = created.getBody().getId();

        OrderStatusUpdate statusUpdate = OrderStatusUpdate.builder()
                .status(OrderStatus.CONFIRMED).build();

        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(statusUpdate, authHeaders(supplierToken)),
                OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void listOrders_asBuyer_returnsBuyerOrders() {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(3).build();
        restTemplate.exchange("/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)), OrderResponse.class);

        ResponseEntity<OrderResponse[]> response = restTemplate.exchange(
                "/api/orders", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                OrderResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    /**
     * Registers a company via the auth endpoint and returns the JWT token.
     *
     * @param name  the company name
     * @param email the email to register with
     * @param role  the company role
     * @return the JWT token
     */
    private String registerAndGetToken(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        return response.getBody().getToken();
    }

    /**
     * Creates HTTP headers with Bearer authentication.
     *
     * @param token the JWT token
     * @return headers with Authorization set
     */
    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
