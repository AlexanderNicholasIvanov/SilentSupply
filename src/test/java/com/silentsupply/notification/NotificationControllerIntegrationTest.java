package com.silentsupply.notification;

import com.silentsupply.attachment.AttachmentRepository;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.notification.dto.NotificationResponse;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.order.OrderStatus;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.order.dto.OrderStatusUpdate;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link NotificationController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NotificationControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private RfqRepository rfqRepository;

    @Autowired
    private CatalogOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        attachmentRepository.deleteAll();
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("NotifSupplier", "notif-supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("NotifBuyer", "notif-buyer@example.com", CompanyRole.BUYER);
    }

    @Test
    void orderStatusChange_createsNotifications() {
        Long productId = createProduct();
        Long orderId = placeOrder(productId);

        // Confirm order (triggers notification)
        OrderStatusUpdate update = OrderStatusUpdate.builder().status(OrderStatus.CONFIRMED).build();
        restTemplate.exchange("/api/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(update, authHeaders(supplierToken)), OrderResponse.class);

        // Both buyer and supplier should have notifications
        ResponseEntity<NotificationResponse[]> buyerNotifs = restTemplate.exchange(
                "/api/notifications", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                NotificationResponse[].class);

        assertThat(buyerNotifs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(buyerNotifs.getBody()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void unreadCount_returnsCorrectCount() {
        Long productId = createProduct();
        Long orderId = placeOrder(productId);

        OrderStatusUpdate update = OrderStatusUpdate.builder().status(OrderStatus.CONFIRMED).build();
        restTemplate.exchange("/api/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(update, authHeaders(supplierToken)), OrderResponse.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/notifications/unread-count", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) response.getBody().get("unreadCount")).longValue()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void markAsRead_marksNotification() {
        Long productId = createProduct();
        Long orderId = placeOrder(productId);

        OrderStatusUpdate update = OrderStatusUpdate.builder().status(OrderStatus.CONFIRMED).build();
        restTemplate.exchange("/api/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(update, authHeaders(supplierToken)), OrderResponse.class);

        ResponseEntity<NotificationResponse[]> notifs = restTemplate.exchange(
                "/api/notifications?unreadOnly=true", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                NotificationResponse[].class);

        Long notifId = notifs.getBody()[0].getId();

        ResponseEntity<NotificationResponse> markResponse = restTemplate.exchange(
                "/api/notifications/" + notifId + "/read", HttpMethod.PATCH,
                new HttpEntity<>(authHeaders(buyerToken)),
                NotificationResponse.class);

        assertThat(markResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(markResponse.getBody().isRead()).isTrue();
    }

    @Test
    void markAllAsRead_clearsUnread() {
        Long productId = createProduct();
        Long orderId = placeOrder(productId);

        OrderStatusUpdate update = OrderStatusUpdate.builder().status(OrderStatus.CONFIRMED).build();
        restTemplate.exchange("/api/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(update, authHeaders(supplierToken)), OrderResponse.class);

        restTemplate.exchange("/api/notifications/read-all", HttpMethod.PATCH,
                new HttpEntity<>(authHeaders(buyerToken)), Void.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/notifications/unread-count", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                Map.class);

        assertThat(((Number) response.getBody().get("unreadCount")).longValue()).isZero();
    }

    private Long createProduct() {
        ProductRequest request = ProductRequest.builder()
                .name("NotifWidget").description("Test").category("Electronics").sku("NW-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .build();
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                ProductResponse.class);
        return response.getBody().getId();
    }

    private Long placeOrder(Long productId) {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(5).build();
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                OrderResponse.class);
        return response.getBody().getId();
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
