package com.silentsupply.negotiation;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NegotiationRuleService}.
 */
@ExtendWith(MockitoExtension.class)
class NegotiationRuleServiceTest {

    @Mock
    private NegotiationRuleRepository ruleRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private NegotiationRuleMapper ruleMapper;

    @InjectMocks
    private NegotiationRuleService ruleService;

    private Company supplier;
    private Product product;
    private NegotiationRuleRequest request;

    @BeforeEach
    void setUp() {
        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);

        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        request = NegotiationRuleRequest.builder()
                .productId(10L).priceFloor(new BigDecimal("7.00"))
                .autoAcceptThreshold(new BigDecimal("9.50")).maxDeliveryDays(30)
                .maxRounds(3).volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();
    }

    @Test
    void create_withValidRequest_createsRule() {
        NegotiationRule rule = NegotiationRule.builder()
                .supplier(supplier).product(product)
                .priceFloor(new BigDecimal("7.00")).autoAcceptThreshold(new BigDecimal("9.50"))
                .maxDeliveryDays(30).maxRounds(3)
                .volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();
        rule.setId(50L);

        NegotiationRuleResponse expectedResponse = NegotiationRuleResponse.builder()
                .id(50L).supplierId(1L).productId(10L).productName("Widget")
                .priceFloor(new BigDecimal("7.00")).autoAcceptThreshold(new BigDecimal("9.50"))
                .maxDeliveryDays(30).maxRounds(3)
                .volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(ruleRepository.findBySupplierIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(ruleMapper.toEntity(request)).thenReturn(rule);
        when(ruleRepository.save(rule)).thenReturn(rule);
        when(ruleMapper.toResponse(rule)).thenReturn(expectedResponse);

        NegotiationRuleResponse result = ruleService.create(1L, request);

        assertThat(result.getPriceFloor()).isEqualByComparingTo(new BigDecimal("7.00"));
        verify(ruleRepository).save(rule);
    }

    @Test
    void create_forOtherSuppliersProduct_throwsAccessDeniedException() {
        Company other = Company.builder().name("Other").email("o@o.com").password("p").role(CompanyRole.SUPPLIER).build();
        other.setId(999L);

        when(companyRepository.findById(999L)).thenReturn(Optional.of(other));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> ruleService.create(999L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("your own products");
    }

    @Test
    void create_withDuplicateRule_throwsBusinessRuleException() {
        NegotiationRule existingRule = NegotiationRule.builder().build();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(ruleRepository.findBySupplierIdAndProductId(1L, 10L)).thenReturn(Optional.of(existingRule));

        assertThatThrownBy(() -> ruleService.create(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");

        verify(ruleRepository, never()).save(any());
    }

    @Test
    void create_withFloorAboveThreshold_throwsBusinessRuleException() {
        request.setPriceFloor(new BigDecimal("15.00"));
        request.setAutoAcceptThreshold(new BigDecimal("9.50"));

        when(companyRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(ruleRepository.findBySupplierIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleService.create(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Price floor must not exceed");
    }
}
