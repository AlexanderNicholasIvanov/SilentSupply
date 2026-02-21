package com.silentsupply.proposal;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
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
 * REST controller for proposal operations within RFQ negotiations.
 */
@RestController
@RequestMapping("/api/rfqs/{rfqId}/proposals")
@RequiredArgsConstructor
@Tag(name = "Proposals", description = "Proposal creation and listing within RFQs")
public class ProposalController {

    private final ProposalService proposalService;

    /**
     * Creates a new buyer proposal for an RFQ.
     *
     * @param rfqId       the RFQ ID
     * @param userDetails the authenticated buyer
     * @param request     the proposal details
     * @return the created proposal with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a buyer proposal for an RFQ (buyer only)")
    public ResponseEntity<ProposalResponse> createProposal(
            @PathVariable Long rfqId,
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody ProposalRequest request) {
        ProposalResponse response = proposalService.createBuyerProposal(
                rfqId, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all proposals for an RFQ, ordered by round number.
     *
     * @param rfqId the RFQ ID
     * @return list of proposals
     */
    @GetMapping
    @Operation(summary = "List all proposals for an RFQ")
    public ResponseEntity<List<ProposalResponse>> listProposals(@PathVariable Long rfqId) {
        return ResponseEntity.ok(proposalService.listByRfq(rfqId));
    }
}
