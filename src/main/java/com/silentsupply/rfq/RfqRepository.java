package com.silentsupply.rfq;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data repository for {@link Rfq} entities.
 */
@Repository
public interface RfqRepository extends JpaRepository<Rfq, Long> {

    /**
     * Finds all RFQs submitted by a specific buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of RFQs
     */
    List<Rfq> findByBuyerId(Long buyerId);

    /**
     * Finds all RFQs directed to a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of RFQs
     */
    List<Rfq> findBySupplierId(Long supplierId);

    /**
     * Finds RFQs that have expired but are still in an active status.
     *
     * @param now            the current timestamp
     * @param activeStatuses the statuses considered active
     * @return list of expired RFQs
     */
    List<Rfq> findByExpiresAtBeforeAndStatusIn(LocalDateTime now, List<RfqStatus> activeStatuses);
}
