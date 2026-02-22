package com.silentsupply;

import com.silentsupply.attachment.AttachmentRepository;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.negotiation.NegotiationRuleRepository;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.order.OrderStatus;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.order.dto.OrderStatusUpdate;
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
 * End-to-end integration test covering the full SilentSupply workflow:
 * <ol>
 *   <li>Register supplier and buyer</li>
 *   <li>Supplier lists a product and sets negotiation rules</li>
 *   <li>Buyer places a catalog order (direct purchase)</li>
 *   <li>Supplier confirms the catalog order</li>
 *   <li>Buyer submits an RFQ for a bulk order</li>
 *   <li>Negotiation engine auto-counters the first proposal</li>
 *   <li>Buyer accepts the counter-offer, engine auto-accepts</li>
 *   <li>Verify final state of all entities</li>
 * </ol>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EndToEndIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AttachmentRepository attachmentRepository;
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

    @BeforeEach
    void cleanAll() {
        attachmentRepository.deleteAll();
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        ruleRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void fullWorkflow_catalogOrderAndRfqWithNegotiation() {
        // === Step 1: Register supplier and buyer ===
        AuthResponse supplierAuth = registerCompany(
                "MegaSupplier Inc.", "supplier@megasupply.com", CompanyRole.SUPPLIER);
        String supplierToken = supplierAuth.getToken();
        Long supplierId = supplierAuth.getCompanyId();

        AuthResponse buyerAuth = registerCompany(
                "GlobalBuyer Corp.", "buyer@globalbuyer.com", CompanyRole.BUYER);
        String buyerToken = buyerAuth.getToken();

        assertThat(supplierAuth.getRole()).isEqualTo("SUPPLIER");
        assertThat(buyerAuth.getRole()).isEqualTo("BUYER");

        // === Step 2: Supplier lists a product ===
        ProductRequest productReq = ProductRequest.builder()
                .name("Industrial Bearing XL-500")
                .description("High-performance industrial bearing for heavy machinery")
                .category("Industrial Parts")
                .sku("BRG-XL-500")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("25.00"))
                .availableQuantity(500)
                .build();

        ResponseEntity<ProductResponse> productResp = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productReq, authHeaders(supplierToken)),
                ProductResponse.class);

        assertThat(productResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long productId = productResp.getBody().getId();
        assertThat(productResp.getBody().getSupplierName()).isEqualTo("MegaSupplier Inc.");

        // === Step 3: Supplier sets negotiation rules ===
        NegotiationRuleRequest ruleReq = NegotiationRuleRequest.builder()
                .productId(productId)
                .priceFloor(new BigDecimal("18.00"))
                .autoAcceptThreshold(new BigDecimal("23.00"))
                .maxDeliveryDays(45)
                .maxRounds(3)
                .volumeDiscountPct(new BigDecimal("10.00"))
                .volumeThreshold(200)
                .build();

        ResponseEntity<NegotiationRuleResponse> ruleResp = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(ruleReq, authHeaders(supplierToken)),
                NegotiationRuleResponse.class);

        assertThat(ruleResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(ruleResp.getBody().getProductName()).isEqualTo("Industrial Bearing XL-500");

        // === Step 4: Buyer places a catalog order (direct purchase) ===
        OrderRequest orderReq = OrderRequest.builder()
                .productId(productId)
                .quantity(10)
                .build();

        ResponseEntity<OrderResponse> orderResp = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(orderReq, authHeaders(buyerToken)),
                OrderResponse.class);

        assertThat(orderResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orderResp.getBody().getQuantity()).isEqualTo(10);
        assertThat(orderResp.getBody().getUnitPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(orderResp.getBody().getTotalPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(orderResp.getBody().getStatus()).isEqualTo(OrderStatus.PLACED);
        Long orderId = orderResp.getBody().getId();

        // Supplier confirms the order
        OrderStatusUpdate confirmUpdate = OrderStatusUpdate.builder()
                .status(OrderStatus.CONFIRMED).build();
        ResponseEntity<OrderResponse> confirmedResp = restTemplate.exchange(
                "/api/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(confirmUpdate, authHeaders(supplierToken)),
                OrderResponse.class);
        assertThat(confirmedResp.getBody().getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // === Step 5: Buyer submits an RFQ for a bulk order ===
        RfqRequest rfqReq = RfqRequest.builder()
                .productId(productId)
                .desiredQuantity(250)
                .targetPrice(new BigDecimal("20.00"))
                .deliveryDeadline(LocalDate.now().plusDays(60))
                .notes("Need bulk order for Q3 production run")
                .build();

        ResponseEntity<RfqResponse> rfqResp = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(rfqReq, authHeaders(buyerToken)),
                RfqResponse.class);

        assertThat(rfqResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long rfqId = rfqResp.getBody().getId();
        assertThat(rfqResp.getBody().getStatus()).isEqualTo(RfqStatus.SUBMITTED);

        // === Step 6: Buyer submits first proposal -- price in negotiable range ===
        // Volume discount applies: 10% off -> floor=16.20, threshold=20.70
        // Price 20.00 is below 20.70 threshold but above 16.20 floor -> COUNTERED
        ProposalRequest proposal1 = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("20.00"))
                .proposedQty(250)
                .deliveryDays(30)
                .build();

        ResponseEntity<ProposalResponse> prop1Resp = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposal1, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(prop1Resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(prop1Resp.getBody().getStatus()).isEqualTo(ProposalStatus.COUNTERED);
        assertThat(prop1Resp.getBody().getReasonCode()).isEqualTo("AUTO_COUNTERED");

        // Verify RFQ is in COUNTERED state
        RfqResponse rfqAfterCounter = getRfq(rfqId, buyerToken);
        assertThat(rfqAfterCounter.getStatus()).isEqualTo(RfqStatus.COUNTERED);
        assertThat(rfqAfterCounter.getCurrentRound()).isEqualTo(1);

        // Verify counter-proposal was generated
        ResponseEntity<ProposalResponse[]> proposalsResp = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProposalResponse[].class);
        assertThat(proposalsResp.getBody()).hasSize(2);

        ProposalResponse counterProp = proposalsResp.getBody()[1];
        assertThat(counterProp.getProposerType()).isEqualTo(ProposerType.SYSTEM);
        assertThat(counterProp.getProposedPrice()).isEqualByComparingTo(new BigDecimal("20.70"));

        // === Step 7: Buyer accepts the counter by proposing at the counter price ===
        ProposalRequest proposal2 = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("20.70"))
                .proposedQty(250)
                .deliveryDays(30)
                .build();

        ResponseEntity<ProposalResponse> prop2Resp = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposal2, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(prop2Resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(prop2Resp.getBody().getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(prop2Resp.getBody().getReasonCode()).isEqualTo("AUTO_ACCEPTED");

        // === Step 8: Verify final state ===
        RfqResponse finalRfq = getRfq(rfqId, buyerToken);
        assertThat(finalRfq.getStatus()).isEqualTo(RfqStatus.ACCEPTED);
        assertThat(finalRfq.getCurrentRound()).isEqualTo(2);

        // Verify product stock was reduced by the catalog order
        ResponseEntity<ProductResponse> finalProduct = restTemplate.exchange(
                "/api/products/" + productId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                ProductResponse.class);
        assertThat(finalProduct.getBody().getAvailableQuantity()).isEqualTo(490);

        // Verify total proposals: 2 buyer + 1 system counter = 3
        ResponseEntity<ProposalResponse[]> finalProposals = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProposalResponse[].class);
        assertThat(finalProposals.getBody()).hasSize(3);
    }

    /**
     * Registers a company and returns the full auth response with token and company info.
     *
     * @param name  the company name
     * @param email the email to register with
     * @param role  the company role (SUPPLIER or BUYER)
     * @return the auth response containing JWT token, company ID, and role
     */
    private AuthResponse registerCompany(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        return restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class).getBody();
    }

    /**
     * Retrieves an RFQ by ID using the given auth token.
     *
     * @param rfqId the RFQ ID to retrieve
     * @param token the JWT token for authentication
     * @return the RFQ response
     */
    private RfqResponse getRfq(Long rfqId, String token) {
        return restTemplate.exchange(
                "/api/rfqs/" + rfqId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                RfqResponse.class).getBody();
    }

    /**
     * Creates HTTP headers with Bearer authentication.
     *
     * @param token the JWT token
     * @return headers with the Authorization header set
     */
    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
