package com.silentsupply.company.dto;

import com.silentsupply.company.CompanyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a company. Excludes sensitive fields like password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    /** Company ID. */
    private Long id;

    /** Business name. */
    private String name;

    /** Contact email. */
    private String email;

    /** Role in the marketplace. */
    private CompanyRole role;

    /** Contact phone number. */
    private String contactPhone;

    /** Business address. */
    private String address;

    /** Whether the company is verified. */
    private boolean verified;

    /** When the company was registered. */
    private LocalDateTime createdAt;
}
