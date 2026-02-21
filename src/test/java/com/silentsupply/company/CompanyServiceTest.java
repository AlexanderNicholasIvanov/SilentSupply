package com.silentsupply.company;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CompanyService}.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRequest request;
    private Company company;
    private CompanyResponse response;

    @BeforeEach
    void setUp() {
        request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .build();

        company = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("encoded-password")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .build();
        company.setId(1L);
        company.setCreatedAt(LocalDateTime.now());

        response = CompanyResponse.builder()
                .id(1L)
                .name("Acme Corp")
                .email("acme@example.com")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .verified(false)
                .createdAt(company.getCreatedAt())
                .build();
    }

    @Test
    void register_withValidRequest_savesAndReturnsResponse() {
        when(companyRepository.existsByEmail("acme@example.com")).thenReturn(false);
        when(companyMapper.toEntity(request)).thenReturn(company);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toResponse(company)).thenReturn(response);

        CompanyResponse result = companyService.register(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Acme Corp");
        assertThat(result.getEmail()).isEqualTo("acme@example.com");
        assertThat(result.getRole()).isEqualTo(CompanyRole.SUPPLIER);
        verify(companyRepository).save(company);
    }

    @Test
    void register_withDuplicateEmail_throwsBusinessRuleException() {
        when(companyRepository.existsByEmail("acme@example.com")).thenReturn(true);

        assertThatThrownBy(() -> companyService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Email already registered");

        verify(companyRepository, never()).save(any());
    }

    @Test
    void getById_withExistingId_returnsResponse() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyMapper.toResponse(company)).thenReturn(response);

        CompanyResponse result = companyService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Acme Corp");
    }

    @Test
    void getById_withNonExistingId_throwsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");
    }

    @Test
    void listAll_returnsAllCompanies() {
        Company buyer = Company.builder()
                .name("BuyerCo")
                .email("buyer@example.com")
                .password("encoded")
                .role(CompanyRole.BUYER)
                .build();
        buyer.setId(2L);
        buyer.setCreatedAt(LocalDateTime.now());

        CompanyResponse buyerResponse = CompanyResponse.builder()
                .id(2L)
                .name("BuyerCo")
                .email("buyer@example.com")
                .role(CompanyRole.BUYER)
                .build();

        when(companyRepository.findAll()).thenReturn(List.of(company, buyer));
        when(companyMapper.toResponse(company)).thenReturn(response);
        when(companyMapper.toResponse(buyer)).thenReturn(buyerResponse);

        List<CompanyResponse> result = companyService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Acme Corp");
        assertThat(result.get(1).getName()).isEqualTo("BuyerCo");
    }
}
