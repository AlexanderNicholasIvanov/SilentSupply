package com.silentsupply.config;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtTokenProvider}.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-that-must-be-at-least-256-bits-long-for-hs256-signing-algo";
        tokenProvider = new JwtTokenProvider(secret, 3600000L);
    }

    @Test
    void generateToken_createsValidToken() {
        CompanyUserDetails userDetails = createUserDetails();

        String token = tokenProvider.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        CompanyUserDetails userDetails = createUserDetails();
        String token = tokenProvider.generateToken(userDetails);

        String email = tokenProvider.getEmailFromToken(token);

        assertThat(email).isEqualTo("acme@example.com");
    }

    @Test
    void getCompanyIdFromToken_returnsCorrectId() {
        CompanyUserDetails userDetails = createUserDetails();
        String token = tokenProvider.generateToken(userDetails);

        Long companyId = tokenProvider.getCompanyIdFromToken(token);

        assertThat(companyId).isEqualTo(1L);
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("not-a-real-token")).isFalse();
    }

    @Test
    void validateToken_withTamperedToken_returnsFalse() {
        CompanyUserDetails userDetails = createUserDetails();
        String token = tokenProvider.generateToken(userDetails);

        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(tokenProvider.validateToken(tampered)).isFalse();
    }

    private CompanyUserDetails createUserDetails() {
        Company company = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("encoded-password")
                .role(CompanyRole.SUPPLIER)
                .build();
        company.setId(1L);
        return new CompanyUserDetails(company);
    }
}
