package com.silentsupply.product;

import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
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
 * Integration tests for {@link ProductController}.
 * Tests CRUD operations and supplier-only access enforcement.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("BuyerCo", "buyer@example.com", CompanyRole.BUYER);
    }

    @Test
    void create_asSupplier_returns201() {
        ProductRequest request = buildProductRequest("Widget A", "WDG-001");

        ResponseEntity<ProductResponse> response = postProduct(request, supplierToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Widget A");
        assertThat(response.getBody().getSupplierId()).isNotNull();
    }

    @Test
    void create_asBuyer_returns403() {
        ProductRequest request = buildProductRequest("Widget A", "WDG-001");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void create_withoutAuth_returns403() {
        ProductRequest request = buildProductRequest("Widget A", "WDG-001");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/products", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getById_returnsProduct() {
        ProductRequest request = buildProductRequest("Widget A", "WDG-001");
        ResponseEntity<ProductResponse> created = postProduct(request, supplierToken);
        Long id = created.getBody().getId();

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Widget A");
    }

    @Test
    void search_byCategory_returnsFilteredResults() {
        postProduct(buildProductRequest("Widget A", "WDG-001"), supplierToken);

        ProductRequest otherProduct = ProductRequest.builder()
                .name("Gadget B")
                .category("Hardware")
                .sku("GDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("49.99"))
                .availableQuantity(50)
                .build();
        postProduct(otherProduct, supplierToken);

        ResponseEntity<ProductResponse[]> response = restTemplate.exchange(
                "/api/products?category=Electronics", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                ProductResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getCategory()).isEqualTo("Electronics");
    }

    @Test
    void delete_byOwningSupplier_returns204() {
        ResponseEntity<ProductResponse> created = postProduct(
                buildProductRequest("Widget A", "WDG-001"), supplierToken);
        Long id = created.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(supplierToken)),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_asBuyer_returns403() {
        ResponseEntity<ProductResponse> created = postProduct(
                buildProductRequest("Widget A", "WDG-001"), supplierToken);
        Long id = created.getBody().getId();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void update_asSupplier_returns200() {
        ResponseEntity<ProductResponse> created = postProduct(
                buildProductRequest("Widget A", "WDG-001"), supplierToken);
        Long id = created.getBody().getId();

        ProductRequest updateRequest = buildProductRequest("Widget A Updated", "WDG-001");

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.PUT,
                new HttpEntity<>(updateRequest, authHeaders(supplierToken)),
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Widget A Updated");
    }

    @Test
    void buyerCanReadProducts() {
        ResponseEntity<ProductResponse> created = postProduct(
                buildProductRequest("Widget A", "WDG-001"), supplierToken);
        Long id = created.getBody().getId();

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Widget A");
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
                .name(name)
                .email(email)
                .password("password123")
                .role(role)
                .build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().getToken();
    }

    /**
     * Builds a standard product request for testing.
     *
     * @param name the product name
     * @param sku  the product SKU
     * @return the product request
     */
    private ProductRequest buildProductRequest(String name, String sku) {
        return ProductRequest.builder()
                .name(name)
                .description("Test product")
                .category("Electronics")
                .sku(sku)
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .build();
    }

    /**
     * Posts a product using the given auth token.
     *
     * @param request the product request
     * @param token   the JWT token
     * @return the response entity
     */
    private ResponseEntity<ProductResponse> postProduct(ProductRequest request, String token) {
        return restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                ProductResponse.class);
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
