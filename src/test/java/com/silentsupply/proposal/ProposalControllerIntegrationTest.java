package com.silentsupply.proposal;

import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProposalController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProposalControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProposalRepository proposalRepository;
    @Autowired
    private RfqRepository rfqRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;
    private Long rfqId;

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
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
        Long productId = productResponse.getBody().getId();

        RfqRequest rfqRequest = RfqRequest.builder()
                .productId(productId).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.now().plusDays(30)).build();
        ResponseEntity<RfqResponse> rfqResponse = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(rfqRequest, authHeaders(buyerToken)),
                RfqResponse.class);
        rfqId = rfqResponse.getBody().getId();
    }

    @Test
    void createProposal_asBuyer_returns201() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProposerType()).isEqualTo(ProposerType.BUYER);
        assertThat(response.getBody().getRoundNumber()).isEqualTo(1);
    }

    @Test
    void createProposal_asSupplier_returns403() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void listProposals_afterCreation_returnsProposals() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();
        restTemplate.exchange("/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)), ProposalResponse.class);

        ResponseEntity<ProposalResponse[]> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProposalResponse[].class);

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
