package com.silentsupply.config.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for login authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    /** The company's email address. */
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    /** The company's password. */
    @NotBlank(message = "Password is required")
    private String password;
}
