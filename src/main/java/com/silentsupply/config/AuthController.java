package com.silentsupply.config;

import com.silentsupply.common.exception.AuthenticationFailedException;
import com.silentsupply.company.CompanyService;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import com.silentsupply.config.dto.AuthRequest;
import com.silentsupply.config.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations: registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login to obtain JWT tokens")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CompanyService companyService;

    /**
     * Registers a new company and returns a JWT token.
     *
     * @param request the company registration details
     * @return the JWT token and company info
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new company and get a JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse company = companyService.register(request);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        CompanyUserDetails userDetails = (CompanyUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .companyId(company.getId())
                .email(company.getEmail())
                .role(company.getRole().name())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Authenticates a company with email and password, returns a JWT token.
     *
     * @param request the login credentials
     * @return the JWT token and company info
     * @throws AuthenticationFailedException if credentials are invalid
     */
    @PostMapping("/login")
    @Operation(summary = "Login with email and password to get a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            CompanyUserDetails userDetails = (CompanyUserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .companyId(userDetails.getId())
                    .email(userDetails.getEmail())
                    .role(userDetails.getRole())
                    .build();

            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }
}
