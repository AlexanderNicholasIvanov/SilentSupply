package com.silentsupply.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data repository for {@link Product} entities.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds all products owned by a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of products for that supplier
     */
    List<Product> findBySupplierId(Long supplierId);

    /**
     * Searches products with optional filters. All parameters are nullable — null means no filter.
     *
     * @param category    category filter (exact match)
     * @param name        name filter (case-insensitive contains)
     * @param minPrice    minimum base price
     * @param maxPrice    maximum base price
     * @param status      product status filter
     * @return list of matching products
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:minPrice IS NULL OR p.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.basePrice <= :maxPrice) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<Product> search(@Param("category") String category,
                         @Param("name") String name,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("status") ProductStatus status);
}
