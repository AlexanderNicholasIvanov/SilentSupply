package com.silentsupply.rfq;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for RFQ operations.
 * Buyers submit RFQs; both buyers and suppliers can view them.
 */
@RestController
@RequestMapping("/api/rfqs")
@RequiredArgsConstructor
@Tag(name = "RFQs", description = "Request for Quote submission and management")
public class RfqController {

    private final RfqService rfqService;

    /**
     * Submits a new RFQ. Buyer-only.
     *
     * @param userDetails the authenticated buyer
     * @param request     the RFQ details
     * @return the created RFQ with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Submit a new RFQ (buyer only)")
    public ResponseEntity<RfqResponse> submit(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody RfqRequest request) {
        RfqResponse response = rfqService.submit(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an RFQ by its ID.
     *
     * @param id the RFQ ID
     * @return the RFQ details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get RFQ by ID")
    public ResponseEntity<RfqResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rfqService.getById(id));
    }

    /**
     * Lists RFQs for the authenticated user (buyer or supplier).
     *
     * @param userDetails the authenticated user
     * @return list of RFQs
     */
    @GetMapping
    @Operation(summary = "List RFQs for authenticated user")
    public ResponseEntity<List<RfqResponse>> listRfqs(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        List<RfqResponse> rfqs;
        if ("SUPPLIER".equals(userDetails.getRole())) {
            rfqs = rfqService.listBySupplier(userDetails.getId());
        } else {
            rfqs = rfqService.listByBuyer(userDetails.getId());
        }
        return ResponseEntity.ok(rfqs);
    }
}
