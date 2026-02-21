package com.silentsupply.negotiation;

import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
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
import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.proposal.ProposerType;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqStatus;
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
 * Integration test for the full RFQ -> Proposal -> Auto-Negotiation flow.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NegotiationIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProposalRepository proposalRepository;
    @Autowired
    private RfqRepository rfqRepository;
    @Autowired
    private NegotiationRuleRepository ruleRepository;
    @Autowired
    private CatalogOrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;
    private Long supplierId;
    private Long productId;

    @BeforeEach
    void setUp() {
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

        ProductRequest productReq = ProductRequest.builder()
                .name("Widget").description("Test").category("Electronics").sku("W-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(1000)
                .build();
        ResponseEntity<ProductResponse> productResp = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productReq, authHeaders(supplierToken)),
                ProductResponse.class);
        productId = productResp.getBody().getId();

        NegotiationRuleRequest ruleReq = NegotiationRuleRequest.builder()
                .productId(productId).priceFloor(new BigDecimal("7.00"))
                .autoAcceptThreshold(new BigDecimal("9.50")).maxDeliveryDays(30)
                .maxRounds(3).volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100)
                .build();
        restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(ruleReq, authHeaders(supplierToken)),
                NegotiationRuleResponse.class);
    }

    @Test
    void fullFlow_proposalAboveThreshold_autoAccepts() {
        Long rfqId = submitRfq();

        ProposalRequest proposalReq = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("9.50")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposalReq, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(response.getBody().getReasonCode()).isEqualTo("AUTO_ACCEPTED");

        RfqResponse rfq = getRfq(rfqId);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.ACCEPTED);
    }

    @Test
    void fullFlow_proposalInNegotiableRange_autoCounters() {
        Long rfqId = submitRfq();

        ProposalRequest proposalReq = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.00")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposalReq, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(ProposalStatus.COUNTERED);

        ResponseEntity<ProposalResponse[]> proposals = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProposalResponse[].class);

        assertThat(proposals.getBody()).hasSize(2);
        ProposalResponse counterProposal = proposals.getBody()[1];
        assertThat(counterProposal.getProposerType()).isEqualTo(ProposerType.SYSTEM);
        assertThat(counterProposal.getProposedPrice()).isEqualByComparingTo(new BigDecimal("9.50"));

        RfqResponse rfq = getRfq(rfqId);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.COUNTERED);
    }

    @Test
    void fullFlow_proposalBelowFloor_autoRejects() {
        Long rfqId = submitRfq();

        ProposalRequest proposalReq = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("5.00")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposalReq, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(response.getBody().getReasonCode()).isEqualTo("PRICE_BELOW_FLOOR");

        RfqResponse rfq = getRfq(rfqId);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.REJECTED);
    }

    @Test
    void fullFlow_volumeDiscount_lowersThresholdAndAccepts() {
        Long rfqId = submitRfqWithQuantity(150);

        ProposalRequest proposalReq = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("9.03")).proposedQty(150).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposalReq, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
    }

    /**
     * Submits an RFQ with default quantity.
     *
     * @return the RFQ ID
     */
    private Long submitRfq() {
        return submitRfqWithQuantity(50);
    }

    /**
     * Submits an RFQ with a specific quantity.
     *
     * @param qty the desired quantity
     * @return the RFQ ID
     */
    private Long submitRfqWithQuantity(int qty) {
        RfqRequest rfqReq = RfqRequest.builder()
                .productId(productId).desiredQuantity(qty).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.now().plusDays(30)).build();
        ResponseEntity<RfqResponse> rfqResp = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(rfqReq, authHeaders(buyerToken)),
                RfqResponse.class);
        return rfqResp.getBody().getId();
    }

    /**
     * Retrieves an RFQ by ID.
     *
     * @param rfqId the RFQ ID
     * @return the RFQ response
     */
    private RfqResponse getRfq(Long rfqId) {
        return restTemplate.exchange(
                "/api/rfqs/" + rfqId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                RfqResponse.class).getBody();
    }

    /**
     * Registers a company and returns the auth response.
     *
     * @param name  the company name
     * @param email the email
     * @param role  the company role
     * @return the auth response
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
