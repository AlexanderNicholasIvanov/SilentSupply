package com.silentsupply.config;

import com.silentsupply.company.Company;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} implementation backed by a {@link Company} entity.
 * The company's role (SUPPLIER/BUYER) is mapped to a Spring Security authority with ROLE_ prefix.
 */
@Getter
public class CompanyUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Creates user details from a Company entity.
     *
     * @param company the company entity
     */
    public CompanyUserDetails(Company company) {
        this.id = company.getId();
        this.email = company.getEmail();
        this.password = company.getPassword();
        this.role = company.getRole().name();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + company.getRole().name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
