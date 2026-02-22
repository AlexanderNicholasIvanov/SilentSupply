package com.silentsupply.negotiation;

import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.notification.NotificationRepository;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.order.CatalogOrderRepository;
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
 * Integration tests for {@link NegotiationRuleController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NegotiationRuleControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NegotiationRuleRepository ruleRepository;
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
    @Autowired
    private NotificationRepository notificationRepository;

    private String supplierToken;
    private String buyerToken;
    private Long supplierId;
    private Long productId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        ruleRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        AuthResponse supplierAuth = registerCompany("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        supplierToken = supplierAuth.getToken();
        supplierId = supplierAuth.getCompanyId();

        buyerToken = registerCompany("BuyerCo", "buyer@example.com", CompanyRole.BUYER).getToken();

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
    void create_asSupplier_returns201() {
        NegotiationRuleRequest request = buildRuleRequest();

        ResponseEntity<NegotiationRuleResponse> response = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                NegotiationRuleResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPriceFloor()).isEqualByComparingTo(new BigDecimal("7.00"));
        assertThat(response.getBody().getProductName()).isEqualTo("Widget");
    }

    @Test
    void create_asBuyer_returns403() {
        NegotiationRuleRequest request = buildRuleRequest();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void list_afterCreation_returnsRules() {
        restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(buildRuleRequest(), authHeaders(supplierToken)),
                NegotiationRuleResponse.class);

        ResponseEntity<NegotiationRuleResponse[]> response = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                NegotiationRuleResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void delete_asSupplier_returns204() {
        ResponseEntity<NegotiationRuleResponse> created = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(buildRuleRequest(), authHeaders(supplierToken)),
                NegotiationRuleResponse.class);
        Long ruleId = created.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules/" + ruleId, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(supplierToken)),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    /**
     * Builds a standard negotiation rule request for testing.
     *
     * @return the rule request
     */
    private NegotiationRuleRequest buildRuleRequest() {
        return NegotiationRuleRequest.builder()
                .productId(productId).priceFloor(new BigDecimal("7.00"))
                .autoAcceptThreshold(new BigDecimal("9.50")).maxDeliveryDays(30)
                .maxRounds(3).volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();
    }

    /**
     * Registers a company and returns the auth response.
     *
     * @param name  the company name
     * @param email the email
     * @param role  the company role
     * @return the auth response with token and company ID
     */
    private AuthResponse registerCompany(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        return restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class).getBody();
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
