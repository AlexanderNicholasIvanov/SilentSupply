package com.silentsupply.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link CatalogOrder} entities.
 */
@Repository
public interface CatalogOrderRepository extends JpaRepository<CatalogOrder, Long> {

    /**
     * Finds all orders placed by a specific buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of orders for that buyer
     */
    List<CatalogOrder> findByBuyerId(Long buyerId);

    /**
     * Finds all orders for a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of orders for that supplier
     */
    List<CatalogOrder> findBySupplierId(Long supplierId);
}
