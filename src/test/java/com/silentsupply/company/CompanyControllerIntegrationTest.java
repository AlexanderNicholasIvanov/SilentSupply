package com.silentsupply.company;

import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import com.silentsupply.config.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

    @BeforeEach
    void cleanUp() {
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
        CompanyRequest request = CompanyRequest.builder()
                .name("BuyerCo")
                .email("buyer@example.com")
                .password("password123")
                .role(CompanyRole.BUYER)
                .build();

        ResponseEntity<CompanyResponse> created = restTemplate.postForEntity(
                "/api/companies", request, CompanyResponse.class);
        Long id = created.getBody().getId();

        ResponseEntity<CompanyResponse> response = restTemplate.getForEntity(
                "/api/companies/" + id, CompanyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("BuyerCo");
    }

    @Test
    void getById_withNonExistingId_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/companies/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listAll_returnsAllRegisteredCompanies() {
        CompanyRequest supplier = CompanyRequest.builder()
                .name("SupplierCo")
                .email("supplier@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();
        CompanyRequest buyer = CompanyRequest.builder()
                .name("BuyerCo")
                .email("buyer2@example.com")
                .password("password123")
                .role(CompanyRole.BUYER)
                .build();

        restTemplate.postForEntity("/api/companies", supplier, CompanyResponse.class);
        restTemplate.postForEntity("/api/companies", buyer, CompanyResponse.class);

        ResponseEntity<CompanyResponse[]> response = restTemplate.getForEntity(
                "/api/companies", CompanyResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}
