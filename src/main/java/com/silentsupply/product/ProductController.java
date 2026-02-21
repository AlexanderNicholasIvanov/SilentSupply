package com.silentsupply.product;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.product.dto.ProductSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for product catalog operations.
 * Suppliers can create, update, and delete their own products.
 * All authenticated users can browse and search products.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog CRUD and search")
public class ProductController {

    private final ProductService productService;

    /**
     * Creates a new product listing. Supplier-only.
     *
     * @param userDetails the authenticated supplier
     * @param request     the product details
     * @return the created product with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a new product (supplier only)")
    public ResponseEntity<ProductResponse> create(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the product details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /**
     * Updates a product listing. Supplier-only, must own the product.
     *
     * @param id          the product ID
     * @param userDetails the authenticated supplier
     * @param request     the updated product details
     * @return the updated product
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a product (supplier only, must own)")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.update(id, userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a product listing. Supplier-only, must own the product.
     *
     * @param id          the product ID
     * @param userDetails the authenticated supplier
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product (supplier only, must own)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        productService.delete(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches products with optional filters.
     *
     * @param category category filter
     * @param name     name filter (case-insensitive contains)
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param status   status filter
     * @return list of matching products
     */
    @GetMapping
    @Operation(summary = "Search products with optional filters")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductStatus status) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .category(category)
                .name(name)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .status(status)
                .build();
        return ResponseEntity.ok(productService.search(criteria));
    }
}
