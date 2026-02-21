package com.silentsupply.company;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for company registration and retrieval.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new company. Encodes the password and persists the entity.
     *
     * @param request the registration request
     * @return the created company as a response DTO
     * @throws BusinessRuleException if the email is already registered
     */
    @Transactional
    public CompanyResponse register(CompanyRequest request) {
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already registered: " + request.getEmail());
        }

        Company company = companyMapper.toEntity(request);
        company.setPassword(passwordEncoder.encode(request.getPassword()));

        Company saved = companyRepository.save(company);
        return companyMapper.toResponse(saved);
    }

    /**
     * Retrieves a company by its ID.
     *
     * @param id the company ID
     * @return the company response DTO
     * @throws ResourceNotFoundException if no company exists with the given ID
     */
    public CompanyResponse getById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        return companyMapper.toResponse(company);
    }

    /**
     * Lists all registered companies.
     *
     * @return list of company response DTOs
     */
    public List<CompanyResponse> listAll() {
        return companyRepository.findAll().stream()
                .map(companyMapper::toResponse)
                .toList();
    }
}
