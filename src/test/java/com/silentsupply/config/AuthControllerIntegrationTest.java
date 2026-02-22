package com.silentsupply.config;

import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.notification.NotificationRepository;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthRequest;
import com.silentsupply.config.dto.AuthResponse;
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
 * Integration tests for {@link AuthController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest extends IntegrationTestBase {

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
    void register_withValidRequest_returns201WithToken() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getCompanyId()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("acme@example.com");
        assertThat(response.getBody().getRole()).isEqualTo("SUPPLIER");
    }

    @Test
    void login_withValidCredentials_returns200WithToken() {
        CompanyRequest registerRequest = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();
        restTemplate.postForEntity("/api/auth/register", registerRequest, AuthResponse.class);

        AuthRequest loginRequest = AuthRequest.builder()
                .email("acme@example.com")
                .password("password123")
                .build();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getEmail()).isEqualTo("acme@example.com");
    }

    @Test
    void login_withInvalidPassword_returns401() {
        CompanyRequest registerRequest = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();
        restTemplate.postForEntity("/api/auth/register", registerRequest, AuthResponse.class);

        AuthRequest loginRequest = AuthRequest.builder()
                .email("acme@example.com")
                .password("wrongpassword")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_withNonExistentEmail_returns401() {
        AuthRequest loginRequest = AuthRequest.builder()
                .email("nobody@example.com")
                .password("password123")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
