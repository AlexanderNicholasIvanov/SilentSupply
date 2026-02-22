package com.silentsupply.attachment;

import com.silentsupply.attachment.dto.AttachmentResponse;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.notification.NotificationRepository;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AttachmentController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AttachmentControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CatalogOrderRepository orderRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private RfqRepository rfqRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private String supplierToken;
    private String buyerToken;
    private Long productId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        attachmentRepository.deleteAll();
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("AttachSupplier", "attach-supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("AttachBuyer", "attach-buyer@example.com", CompanyRole.BUYER);

        productId = createProduct();
    }

    @Test
    void upload_asProductSupplier_returns201() {
        ResponseEntity<AttachmentResponse> response = uploadFile(supplierToken, "PRODUCT", productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFileName()).isEqualTo("spec.pdf");
        assertThat(response.getBody().getEntityType()).isEqualTo(AttachmentEntityType.PRODUCT);
        assertThat(response.getBody().getEntityId()).isEqualTo(productId);
    }

    @Test
    void upload_asNonOwner_returns403() {
        ResponseEntity<String> response = uploadFileRaw(buyerToken, "PRODUCT", productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void listByEntity_returnsAttachments() {
        uploadFile(supplierToken, "PRODUCT", productId);

        ResponseEntity<AttachmentResponse[]> response = restTemplate.exchange(
                "/api/attachments?entityType=PRODUCT&entityId=" + productId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                AttachmentResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void download_existingAttachment_returns200() {
        ResponseEntity<AttachmentResponse> uploaded = uploadFile(supplierToken, "PRODUCT", productId);
        Long attachmentId = uploaded.getBody().getId();

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "/api/attachments/" + attachmentId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void delete_asUploader_returns204() {
        ResponseEntity<AttachmentResponse> uploaded = uploadFile(supplierToken, "PRODUCT", productId);
        Long attachmentId = uploaded.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/attachments/" + attachmentId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(supplierToken)),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_asNonUploader_returns403() {
        ResponseEntity<AttachmentResponse> uploaded = uploadFile(supplierToken, "PRODUCT", productId);
        Long attachmentId = uploaded.getBody().getId();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/attachments/" + attachmentId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<AttachmentResponse> uploadFile(String token, String entityType, Long entityId) {
        HttpHeaders headers = authHeaders(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("test file content".getBytes()) {
            @Override
            public String getFilename() {
                return "spec.pdf";
            }
        });
        body.add("entityType", entityType);
        body.add("entityId", entityId.toString());

        return restTemplate.exchange("/api/attachments", HttpMethod.POST,
                new HttpEntity<>(body, headers), AttachmentResponse.class);
    }

    private ResponseEntity<String> uploadFileRaw(String token, String entityType, Long entityId) {
        HttpHeaders headers = authHeaders(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("test file content".getBytes()) {
            @Override
            public String getFilename() {
                return "spec.pdf";
            }
        });
        body.add("entityType", entityType);
        body.add("entityId", entityId.toString());

        return restTemplate.exchange("/api/attachments", HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);
    }

    private Long createProduct() {
        ProductRequest request = ProductRequest.builder()
                .name("AttachWidget").description("Test").category("Electronics").sku("AW-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .build();
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                ProductResponse.class);
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
