package com.silentsupply.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Security configuration. Enables JWT-based stateless authentication
 * and defines endpoint access rules based on company roles.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain with JWT auth and endpoint rules.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/companies").permitAll()
                .requestMatchers("/", "/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                // Supplier-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("SUPPLIER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SUPPLIER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SUPPLIER")
                .requestMatchers("/api/suppliers/*/negotiation-rules/**").hasRole("SUPPLIER")
                .requestMatchers("/api/analytics/supplier").hasRole("SUPPLIER")
                // Buyer-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/rfqs").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/rfqs/*/proposals").hasRole("BUYER")
                .requestMatchers("/api/analytics/buyer").hasRole("BUYER")
                // Authenticated endpoints
                .requestMatchers("/api/attachments/**").authenticated()
                // All other authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Redirects the root path to Swagger UI for convenience.
     *
     * @param registry the view controller registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/swagger-ui.html");
    }

    /**
     * Exposes the authentication manager for use by the auth controller.
     *
     * @param config the authentication configuration
     * @return the authentication manager
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
