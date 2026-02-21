package com.silentsupply.product;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.product.dto.ProductSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for product catalog CRUD operations and search.
 * Enforces that only the owning supplier can create, update, or delete products.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final ProductMapper productMapper;

    /**
     * Creates a new product listing for the given supplier.
     *
     * @param supplierId the supplier's company ID
     * @param request    the product details
     * @return the created product
     */
    @Transactional
    public ProductResponse create(Long supplierId, ProductRequest request) {
        Company supplier = companyRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", supplierId));

        Product product = productMapper.toEntity(request);
        product.setSupplier(supplier);

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the product details
     * @throws ResourceNotFoundException if no product exists with the given ID
     */
    public ProductResponse getById(Long id) {
        Product product = findProductOrThrow(id);
        return productMapper.toResponse(product);
    }

    /**
     * Updates a product listing. Only the owning supplier can update.
     *
     * @param productId  the product ID to update
     * @param supplierId the requesting supplier's ID
     * @param request    the updated product details
     * @return the updated product
     * @throws AccessDeniedException if the supplier does not own this product
     */
    @Transactional
    public ProductResponse update(Long productId, Long supplierId, ProductRequest request) {
        Product product = findProductOrThrow(productId);
        verifyOwnership(product, supplierId);

        productMapper.updateEntity(request, product);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    /**
     * Deletes a product listing. Only the owning supplier can delete.
     *
     * @param productId  the product ID to delete
     * @param supplierId the requesting supplier's ID
     * @throws AccessDeniedException if the supplier does not own this product
     */
    @Transactional
    public void delete(Long productId, Long supplierId) {
        Product product = findProductOrThrow(productId);
        verifyOwnership(product, supplierId);
        productRepository.delete(product);
    }

    /**
     * Lists all products for a given supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of products for that supplier
     */
    public List<ProductResponse> listBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    /**
     * Searches products using optional filter criteria.
     *
     * @param criteria the search filters
     * @return list of matching products
     */
    public List<ProductResponse> search(ProductSearchCriteria criteria) {
        return productRepository.search(
                criteria.getCategory(),
                criteria.getName(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getStatus()
        ).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    /**
     * Finds a product by ID or throws.
     *
     * @param id the product ID
     * @return the product entity
     */
    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    /**
     * Verifies that the given supplier owns the product.
     *
     * @param product    the product entity
     * @param supplierId the requesting supplier's ID
     */
    private void verifyOwnership(Product product, Long supplierId) {
        if (!product.getSupplier().getId().equals(supplierId)) {
            throw new AccessDeniedException("You can only manage your own products");
        }
    }
}
