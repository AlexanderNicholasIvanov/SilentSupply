package com.silentsupply.company;

import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.notification.NotificationRepository;
import com.silentsupply.config.dto.AuthResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CompanyController}.
 * Uses a real PostgreSQL container and the full Spring context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CompanyControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void cleanUp() {
        notificationRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void register_withValidRequest_returns201() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .build();

        ResponseEntity<CompanyResponse> response = restTemplate.postForEntity(
                "/api/companies", request, CompanyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Acme Corp");
        assertThat(response.getBody().getEmail()).isEqualTo("acme@example.com");
        assertThat(response.getBody().getRole()).isEqualTo(CompanyRole.SUPPLIER);
    }

    @Test
    void register_withDuplicateEmail_returns400() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("dup@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();

        restTemplate.postForEntity("/api/companies", request, CompanyResponse.class);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/companies", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_withMissingName_returns400() {
        CompanyRequest request = CompanyRequest.builder()
                .email("noname@example.com")
                .password("password123")
                .role(CompanyRole.BUYER)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/companies", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getById_withExistingCompany_returns200() {
        String token = registerAndGetToken("buyer@example.com", "password123", CompanyRole.BUYER, "BuyerCo");

        // Find the created company's ID
        CompanyRequest request = CompanyRequest.builder()
                .name("SupplierCo")
                .email("supplier@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();
        ResponseEntity<CompanyResponse> created = restTemplate.postForEntity(
                "/api/companies", request, CompanyResponse.class);
        Long id = created.getBody().getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CompanyResponse> response = restTemplate.exchange(
                "/api/companies/" + id, HttpMethod.GET, entity, CompanyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("SupplierCo");
    }

    @Test
    void getById_withNonExistingId_returns404() {
        String token = registerAndGetToken("auth@example.com", "password123", CompanyRole.SUPPLIER, "AuthCo");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/companies/99999", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listAll_returnsAllRegisteredCompanies() {
        String token = registerAndGetToken("supplier@example.com", "password123", CompanyRole.SUPPLIER, "SupplierCo");

        CompanyRequest buyer = CompanyRequest.builder()
                .name("BuyerCo")
                .email("buyer2@example.com")
                .password("password123")
                .role(CompanyRole.BUYER)
                .build();
        restTemplate.postForEntity("/api/companies", buyer, CompanyResponse.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CompanyResponse[]> response = restTemplate.exchange(
                "/api/companies", HttpMethod.GET, entity, CompanyResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    /**
     * Registers a company via the auth endpoint and returns the JWT token.
     *
     * @param email    the email to register with
     * @param password the password
     * @param role     the company role
     * @param name     the company name
     * @return the JWT token
     */
    private String registerAndGetToken(String email, String password, CompanyRole role, String name) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .build();
        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return authResponse.getBody().getToken();
    }
}
