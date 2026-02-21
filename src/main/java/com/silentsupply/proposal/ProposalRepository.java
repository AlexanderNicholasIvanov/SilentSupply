package com.silentsupply.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link Proposal} entities.
 */
@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    /**
     * Finds all proposals for a given RFQ, ordered by round number then by ID.
     *
     * @param rfqId the RFQ ID
     * @return list of proposals ordered by round then creation order
     */
    List<Proposal> findByRfqIdOrderByRoundNumberAscIdAsc(Long rfqId);
}
