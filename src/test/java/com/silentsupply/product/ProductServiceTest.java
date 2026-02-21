package com.silentsupply.product;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.product.dto.ProductSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductService}.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Company supplier;
    private Product product;
    private ProductRequest request;
    private ProductResponse response;

    @BeforeEach
    void setUp() {
        supplier = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("hashed")
                .role(CompanyRole.SUPPLIER)
                .build();
        supplier.setId(1L);

        product = Product.builder()
                .supplier(supplier)
                .name("Widget A")
                .description("A fine widget")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        product.setId(10L);

        request = ProductRequest.builder()
                .name("Widget A")
                .description("A fine widget")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .build();

        response = ProductResponse.builder()
                .id(10L)
                .supplierId(1L)
                .supplierName("Acme Corp")
                .name("Widget A")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void create_withValidRequest_savesAndReturnsResponse() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.create(1L, request);

        assertThat(result.getName()).isEqualTo("Widget A");
        assertThat(result.getSupplierId()).isEqualTo(1L);
        verify(productRepository).save(product);
    }

    @Test
    void create_withNonExistentSupplier_throwsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getById_withExistingId_returnsResponse() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getById(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getById_withNonExistingId_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_byOwningSupplier_updatesAndReturnsResponse() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.update(10L, 1L, request);

        assertThat(result.getName()).isEqualTo("Widget A");
        verify(productMapper).updateEntity(request, product);
    }

    @Test
    void update_byDifferentSupplier_throwsAccessDeniedException() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.update(10L, 999L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("your own products");
    }

    @Test
    void delete_byOwningSupplier_deletesProduct() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        productService.delete(10L, 1L);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_byDifferentSupplier_throwsAccessDeniedException() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.delete(10L, 999L))
                .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void listBySupplier_returnsProductsForSupplier() {
        when(productRepository.findBySupplierId(1L)).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> result = productService.listBySupplier(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplierId()).isEqualTo(1L);
    }

    @Test
    void search_withCriteria_returnsMatchingProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .category("Electronics")
                .build();

        when(productRepository.search("Electronics", null, null, null, null))
                .thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> result = productService.search(criteria);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
    }
}
