package com.silentsupply.company;

import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CompanyMapper}.
 */
class CompanyMapperTest {

    private final CompanyMapper mapper = Mappers.getMapper(CompanyMapper.class);

    @Test
    void toEntity_mapsAllFieldsExceptIdAndTimestamps() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .build();

        Company company = mapper.toEntity(request);

        assertThat(company.getId()).isNull();
        assertThat(company.getName()).isEqualTo("Acme Corp");
        assertThat(company.getEmail()).isEqualTo("acme@example.com");
        assertThat(company.getPassword()).isEqualTo("password123");
        assertThat(company.getRole()).isEqualTo(CompanyRole.SUPPLIER);
        assertThat(company.getContactPhone()).isEqualTo("+1234567890");
        assertThat(company.getAddress()).isEqualTo("123 Main St");
    }

    @Test
    void toResponse_mapsAllFieldsExcludingPassword() {
        Company company = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("hashed-password")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .verified(true)
                .build();
        company.setId(1L);
        company.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));

        CompanyResponse response = mapper.toResponse(company);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Acme Corp");
        assertThat(response.getEmail()).isEqualTo("acme@example.com");
        assertThat(response.getRole()).isEqualTo(CompanyRole.SUPPLIER);
        assertThat(response.isVerified()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
    }
}
