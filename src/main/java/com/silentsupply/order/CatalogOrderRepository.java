package com.silentsupply.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Returns aggregate revenue stats for a supplier: [[orderCount, totalRevenue]].
     *
     * @param supplierId the supplier's company ID
     * @return list containing a single object array with count and sum of total_price
     */
    @Query("SELECT COUNT(o), COALESCE(SUM(o.totalPrice), 0.0) FROM CatalogOrder o WHERE o.supplier.id = :supplierId")
    List<Object[]> findRevenueStatsForSupplier(@Param("supplierId") Long supplierId);

    /**
     * Returns top revenue-generating products for a supplier: [productId, productName, sumRevenue, orderCount].
     *
     * @param supplierId the supplier's company ID
     * @return list of object arrays ordered by revenue descending
     */
    @Query("SELECT o.product.id, o.product.name, SUM(o.totalPrice), COUNT(o) " +
            "FROM CatalogOrder o WHERE o.supplier.id = :supplierId " +
            "GROUP BY o.product.id, o.product.name ORDER BY SUM(o.totalPrice) DESC")
    List<Object[]> findRevenueByProductForSupplier(@Param("supplierId") Long supplierId);

    /**
     * Returns order counts grouped by status for a supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of [status, count] arrays
     */
    @Query("SELECT o.status, COUNT(o) FROM CatalogOrder o WHERE o.supplier.id = :supplierId GROUP BY o.status")
    List<Object[]> countByStatusForSupplier(@Param("supplierId") Long supplierId);

    /**
     * Returns aggregate spend stats for a buyer: [[orderCount, totalSpend]].
     *
     * @param buyerId the buyer's company ID
     * @return list containing a single object array with count and sum of total_price
     */
    @Query("SELECT COUNT(o), COALESCE(SUM(o.totalPrice), 0.0) FROM CatalogOrder o WHERE o.buyer.id = :buyerId")
    List<Object[]> findSpendStatsForBuyer(@Param("buyerId") Long buyerId);

    /**
     * Returns order counts grouped by status for a buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of [status, count] arrays
     */
    @Query("SELECT o.status, COUNT(o) FROM CatalogOrder o WHERE o.buyer.id = :buyerId GROUP BY o.status")
    List<Object[]> countByStatusForBuyer(@Param("buyerId") Long buyerId);
}
