package com.silentsupply.company;

import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for company registration and retrieval.
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company registration and management")
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Registers a new company in the marketplace.
     *
     * @param request the company registration details
     * @return the created company with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Register a new company")
    public ResponseEntity<CompanyResponse> register(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a company by its ID.
     *
     * @param id the company ID
     * @return the company details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID")
    public ResponseEntity<CompanyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    /**
     * Lists companies, optionally filtered by role.
     *
     * @param role optional role filter (SUPPLIER or BUYER)
     * @return list of companies matching the filter, or all companies if no filter
     */
    @GetMapping
    @Operation(summary = "List companies, optionally filtered by role")
    public ResponseEntity<List<CompanyResponse>> listAll(
            @RequestParam(required = false) CompanyRole role) {
        if (role != null) {
            return ResponseEntity.ok(companyService.listByRole(role));
        }
        return ResponseEntity.ok(companyService.listAll());
    }
}
