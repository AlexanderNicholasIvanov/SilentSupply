package com.silentsupply.negotiation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link NegotiationRule} entities.
 */
@Repository
public interface NegotiationRuleRepository extends JpaRepository<NegotiationRule, Long> {

    /**
     * Finds all negotiation rules for a given supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of rules
     */
    List<NegotiationRule> findBySupplierId(Long supplierId);

    /**
     * Finds the negotiation rule for a specific supplier-product pair.
     *
     * @param supplierId the supplier's company ID
     * @param productId  the product ID
     * @return the rule if found
     */
    Optional<NegotiationRule> findBySupplierIdAndProductId(Long supplierId, Long productId);
}
