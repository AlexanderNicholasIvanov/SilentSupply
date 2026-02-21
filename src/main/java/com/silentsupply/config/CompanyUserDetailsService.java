package com.silentsupply.config;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details from the Company table for Spring Security authentication.
 */
@Service
@RequiredArgsConstructor
public class CompanyUserDetailsService implements UserDetailsService {

    private final CompanyRepository companyRepository;

    /**
     * Loads a company by email for authentication.
     *
     * @param email the company's email address
     * @return the user details
     * @throws UsernameNotFoundException if no company exists with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Company not found with email: " + email));
        return new CompanyUserDetails(company);
    }
}
