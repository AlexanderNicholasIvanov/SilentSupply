package com.silentsupply.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful authentication.
 * Contains the JWT token and basic company info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** The JWT access token. */
    private String token;

    /** The authenticated company's ID. */
    private Long companyId;

    /** The authenticated company's email. */
    private String email;

    /** The authenticated company's role (SUPPLIER or BUYER). */
    private String role;
}
