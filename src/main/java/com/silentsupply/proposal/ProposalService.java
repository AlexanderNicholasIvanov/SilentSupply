package com.silentsupply.proposal;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.negotiation.NegotiationEngine;
import com.silentsupply.negotiation.NegotiationResult;
import com.silentsupply.negotiation.NegotiationRule;
import com.silentsupply.negotiation.NegotiationRuleRepository;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqService;
import com.silentsupply.rfq.RfqStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service layer for proposal creation and retrieval within RFQ negotiations.
 * When a buyer submits a proposal and negotiation rules exist, the negotiation
 * engine is triggered automatically to evaluate and potentially counter or resolve.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProposalService {

    /** RFQ statuses that allow new proposals. */
    private static final Set<RfqStatus> PROPOSABLE_STATUSES = Set.of(
            RfqStatus.SUBMITTED, RfqStatus.UNDER_REVIEW, RfqStatus.COUNTERED);

    private final ProposalRepository proposalRepository;
    private final RfqRepository rfqRepository;
    private final RfqService rfqService;
    private final ProposalMapper proposalMapper;
    private final NegotiationRuleRepository ruleRepository;
    private final NegotiationEngine negotiationEngine;

    /**
     * Creates a buyer proposal for an RFQ. If negotiation rules exist for the product,
     * the negotiation engine evaluates the proposal and may auto-accept, counter, or reject.
     *
     * @param rfqId   the RFQ ID
     * @param buyerId the buyer's company ID
     * @param request the proposal details
     * @return the created proposal (may already be resolved by the engine)
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

        Proposal savedProposal = proposalRepository.save(proposal);

        Optional<NegotiationRule> ruleOpt = ruleRepository.findBySupplierIdAndProductId(
                rfq.getSupplier().getId(), rfq.getProduct().getId());

        if (ruleOpt.isPresent()) {
            NegotiationResult result = negotiationEngine.evaluate(savedProposal, rfq, ruleOpt.get());
            applyNegotiationResult(savedProposal, rfq, result);
        }

        return proposalMapper.toResponse(savedProposal);
    }

    /**
     * Lists all proposals for a given RFQ, ordered by round number.
     *
     * @param rfqId the RFQ ID
     * @return list of proposals
     */
    public List<ProposalResponse> listByRfq(Long rfqId) {
        return proposalRepository.findByRfqIdOrderByRoundNumberAscIdAsc(rfqId).stream()
                .map(proposalMapper::toResponse)
                .toList();
    }

    /**
     * Applies the negotiation engine's result to the proposal and RFQ.
     *
     * @param buyerProposal the buyer's proposal
     * @param rfq           the associated RFQ
     * @param result        the negotiation result
     */
    private void applyNegotiationResult(Proposal buyerProposal, Rfq rfq, NegotiationResult result) {
        buyerProposal.setStatus(result.getBuyerProposalStatus());
        buyerProposal.setReasonCode(result.getReasonCode());
        proposalRepository.save(buyerProposal);

        switch (result.getBuyerProposalStatus()) {
            case ACCEPTED -> {
                rfq.setStatus(RfqStatus.ACCEPTED);
                rfqRepository.save(rfq);
                log.info("RFQ {} auto-accepted at round {}", rfq.getId(), rfq.getCurrentRound());
            }
            case REJECTED -> {
                rfq.setStatus(RfqStatus.REJECTED);
                rfqRepository.save(rfq);
                log.info("RFQ {} auto-rejected: {}", rfq.getId(), result.getReasonCode());
            }
            case COUNTERED -> {
                rfq.setStatus(RfqStatus.COUNTERED);
                rfqRepository.save(rfq);

                Proposal counterProposal = Proposal.builder()
                        .rfq(rfq)
                        .proposerType(ProposerType.SYSTEM)
                        .proposedPrice(result.getCounterPrice())
                        .proposedQty(result.getCounterQty())
                        .deliveryDays(result.getCounterDeliveryDays())
                        .status(ProposalStatus.PENDING)
                        .roundNumber(rfq.getCurrentRound())
                        .reasonCode("AUTO_COUNTERED")
                        .build();
                proposalRepository.save(counterProposal);
                log.info("RFQ {} auto-countered at round {} with price {}",
                        rfq.getId(), rfq.getCurrentRound(), result.getCounterPrice());
            }
            default -> log.warn("Unexpected proposal status from engine: {}", result.getBuyerProposalStatus());
        }
    }
}
