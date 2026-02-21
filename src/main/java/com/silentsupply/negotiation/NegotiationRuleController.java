package com.silentsupply.negotiation;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing negotiation rules.
 * Supplier-only: each supplier manages rules for their own products.
 */
@RestController
@RequestMapping("/api/suppliers/{supplierId}/negotiation-rules")
@RequiredArgsConstructor
@Tag(name = "Negotiation Rules", description = "Supplier-defined negotiation rules per product")
public class NegotiationRuleController {

    private final NegotiationRuleService ruleService;

    /**
     * Creates a new negotiation rule for a product.
     *
     * @param supplierId  the supplier's company ID (from path)
     * @param userDetails the authenticated supplier
     * @param request     the rule details
     * @return the created rule with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a negotiation rule (supplier only)")
    public ResponseEntity<NegotiationRuleResponse> create(
            @PathVariable Long supplierId,
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody NegotiationRuleRequest request) {
        NegotiationRuleResponse response = ruleService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing negotiation rule.
     *
     * @param supplierId  the supplier's company ID
     * @param ruleId      the rule ID
     * @param userDetails the authenticated supplier
     * @param request     the updated rule details
     * @return the updated rule
     */
    @PutMapping("/{ruleId}")
    @Operation(summary = "Update a negotiation rule (supplier only)")
    public ResponseEntity<NegotiationRuleResponse> update(
            @PathVariable Long supplierId,
            @PathVariable Long ruleId,
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody NegotiationRuleRequest request) {
        NegotiationRuleResponse response = ruleService.update(ruleId, userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a negotiation rule.
     *
     * @param supplierId  the supplier's company ID
     * @param ruleId      the rule ID
     * @param userDetails the authenticated supplier
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{ruleId}")
    @Operation(summary = "Delete a negotiation rule (supplier only)")
    public ResponseEntity<Void> delete(
            @PathVariable Long supplierId,
            @PathVariable Long ruleId,
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        ruleService.delete(ruleId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all negotiation rules for the supplier.
     *
     * @param supplierId  the supplier's company ID
     * @param userDetails the authenticated supplier
     * @return list of rules
     */
    @GetMapping
    @Operation(summary = "List all negotiation rules for a supplier")
    public ResponseEntity<List<NegotiationRuleResponse>> listBySupplier(
            @PathVariable Long supplierId,
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        return ResponseEntity.ok(ruleService.listBySupplier(userDetails.getId()));
    }
}
