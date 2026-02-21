package com.silentsupply.proposal;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqService;
import com.silentsupply.rfq.RfqStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Service layer for proposal creation and retrieval within RFQ negotiations.
 * The negotiation engine is wired in during Phase 6.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalService {

    /** RFQ statuses that allow new proposals. */
    private static final Set<RfqStatus> PROPOSABLE_STATUSES = Set.of(
            RfqStatus.SUBMITTED, RfqStatus.UNDER_REVIEW, RfqStatus.COUNTERED);

    private final ProposalRepository proposalRepository;
    private final RfqRepository rfqRepository;
    private final RfqService rfqService;
    private final ProposalMapper proposalMapper;

    /**
     * Creates a buyer proposal for an RFQ. Increments the RFQ round counter
     * and transitions status to UNDER_REVIEW.
     *
     * @param rfqId   the RFQ ID
     * @param buyerId the buyer's company ID
     * @param request the proposal details
     * @return the created proposal
     * @throws BusinessRuleException if the RFQ is not in a proposable status or max rounds exceeded
     */
    @Transactional
    public ProposalResponse createBuyerProposal(Long rfqId, Long buyerId, ProposalRequest request) {
        Rfq rfq = rfqService.findRfqOrThrow(rfqId);

        if (!rfq.getBuyer().getId().equals(buyerId)) {
            throw new BusinessRuleException("Only the RFQ owner can submit proposals");
        }

        if (!PROPOSABLE_STATUSES.contains(rfq.getStatus())) {
            throw new BusinessRuleException("RFQ is not in a status that accepts proposals: " + rfq.getStatus());
        }

        if (rfq.getCurrentRound() >= rfq.getMaxRounds()) {
            throw new BusinessRuleException("Maximum negotiation rounds reached: " + rfq.getMaxRounds());
        }

        int nextRound = rfq.getCurrentRound() + 1;
        rfq.setCurrentRound(nextRound);
        rfq.setStatus(RfqStatus.UNDER_REVIEW);
        rfqRepository.save(rfq);

        Proposal proposal = Proposal.builder()
                .rfq(rfq)
                .proposerType(ProposerType.BUYER)
                .proposedPrice(request.getProposedPrice())
                .proposedQty(request.getProposedQty())
                .deliveryDays(request.getDeliveryDays())
                .status(ProposalStatus.PENDING)
                .roundNumber(nextRound)
                .build();

        Proposal saved = proposalRepository.save(proposal);
        return proposalMapper.toResponse(saved);
    }

    /**
     * Lists all proposals for a given RFQ, ordered by round number.
     *
     * @param rfqId the RFQ ID
     * @return list of proposals
     */
    public List<ProposalResponse> listByRfq(Long rfqId) {
        return proposalRepository.findByRfqIdOrderByRoundNumberAsc(rfqId).stream()
                .map(proposalMapper::toResponse)
                .toList();
    }
}
