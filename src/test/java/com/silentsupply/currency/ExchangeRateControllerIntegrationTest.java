package com.silentsupply.currency;

import com.silentsupply.attachment.AttachmentRepository;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.IntegrationTestBase;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.currency.dto.ExchangeRateRequest;
import com.silentsupply.currency.dto.ExchangeRateResponse;
import com.silentsupply.negotiation.NegotiationRuleRepository;
import com.silentsupply.notification.NotificationRepository;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.product.ProductRepository;
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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ExchangeRateController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExchangeRateControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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

    private String token;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        attachmentRepository.deleteAll();
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        ruleRepository.deleteAll();
        orderRepository.deleteAll();
        exchangeRateRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        token = registerAndGetToken("RateCo", "rate@example.com", CompanyRole.SUPPLIER);
    }

    @Test
    void list_returnsAllRates() {
        seedRate(Currency.USD, Currency.EUR, "0.92000000");

        ResponseEntity<ExchangeRateResponse[]> response = restTemplate.exchange(
                "/api/exchange-rates", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                ExchangeRateResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void list_withFilter_returnsFilteredRates() {
        seedRate(Currency.USD, Currency.EUR, "0.92000000");
        seedRate(Currency.USD, Currency.GBP, "0.79000000");

        ResponseEntity<ExchangeRateResponse[]> response = restTemplate.exchange(
                "/api/exchange-rates?from=USD&to=EUR", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                ExchangeRateResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getFromCurrency()).isEqualTo(Currency.USD);
        assertThat(response.getBody()[0].getToCurrency()).isEqualTo(Currency.EUR);
    }

    @Test
    void createOrUpdate_returns200() {
        ExchangeRateRequest request = ExchangeRateRequest.builder()
                .fromCurrency(Currency.EUR).toCurrency(Currency.GBP)
                .rate(new BigDecimal("0.85870000"))
                .effectiveDate(LocalDate.of(2026, 2, 1))
                .build();

        ResponseEntity<ExchangeRateResponse> response = restTemplate.exchange(
                "/api/exchange-rates", HttpMethod.PUT,
                new HttpEntity<>(request, authHeaders(token)),
                ExchangeRateResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFromCurrency()).isEqualTo(Currency.EUR);
        assertThat(response.getBody().getToCurrency()).isEqualTo(Currency.GBP);
        assertThat(response.getBody().getRate()).isEqualByComparingTo(new BigDecimal("0.85870000"));
    }

    @Test
    void list_withoutAuth_returns403() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/exchange-rates", HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private void seedRate(Currency from, Currency to, String rate) {
        ExchangeRate entity = ExchangeRate.builder()
                .fromCurrency(from).toCurrency(to)
                .rate(new BigDecimal(rate))
                .effectiveDate(LocalDate.of(2026, 1, 1))
                .build();
        exchangeRateRepository.save(entity);
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
