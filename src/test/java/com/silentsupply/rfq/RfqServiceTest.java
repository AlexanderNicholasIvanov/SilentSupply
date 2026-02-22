package com.silentsupply.rfq;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.notification.NotificationService;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RfqService}.
 */
@ExtendWith(MockitoExtension.class)
class RfqServiceTest {

    @Mock
    private RfqRepository rfqRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private RfqMapper rfqMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RfqService rfqService;

    private Company buyer;
    private Company supplier;
    private Product product;

    @BeforeEach
    void setUp() {
        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);

        buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(2L);

        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);
    }

    @Test
    void submit_withValidRequest_createsRfq() {
        RfqRequest request = RfqRequest.builder()
                .productId(10L).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1)).notes("Urgent").build();

        RfqResponse expectedResponse = RfqResponse.builder()
                .id(100L).buyerId(2L).productId(10L).supplierId(1L)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .status(RfqStatus.SUBMITTED).currentRound(0).maxRounds(3).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(rfqRepository.save(any(Rfq.class))).thenAnswer(inv -> {
            Rfq rfq = inv.getArgument(0);
            rfq.setId(100L);
            return rfq;
        });
        when(rfqMapper.toResponse(any(Rfq.class))).thenReturn(expectedResponse);

        RfqResponse result = rfqService.submit(2L, request);

        assertThat(result.getDesiredQuantity()).isEqualTo(50);
        assertThat(result.getStatus()).isEqualTo(RfqStatus.SUBMITTED);
        verify(rfqRepository).save(any(Rfq.class));
    }

    @Test
    void submit_withInactiveProduct_throwsBusinessRuleException() {
        product.setStatus(ProductStatus.DISCONTINUED);
        RfqRequest request = RfqRequest.builder()
                .productId(10L).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1)).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> rfqService.submit(2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive product");
    }

    @Test
    void getById_withExistingId_returnsResponse() {
        Rfq rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .status(RfqStatus.SUBMITTED).currentRound(0).maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        rfq.setId(100L);

        RfqResponse expectedResponse = RfqResponse.builder().id(100L).status(RfqStatus.SUBMITTED).build();

        when(rfqRepository.findById(100L)).thenReturn(Optional.of(rfq));
        when(rfqMapper.toResponse(rfq)).thenReturn(expectedResponse);

        RfqResponse result = rfqService.getById(100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getById_withNonExistingId_throwsResourceNotFoundException() {
        when(rfqRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rfqService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void expireOverdueRfqs_expiresActiveRfqsPastDeadline() {
        Rfq expiredRfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 3, 1))
                .status(RfqStatus.SUBMITTED).currentRound(0).maxRounds(3)
                .expiresAt(LocalDateTime.now().minusDays(1)).build();
        expiredRfq.setId(100L);

        when(rfqRepository.findByExpiresAtBeforeAndStatusIn(any(), any()))
                .thenReturn(List.of(expiredRfq));

        int count = rfqService.expireOverdueRfqs();

        assertThat(count).isEqualTo(1);
        assertThat(expiredRfq.getStatus()).isEqualTo(RfqStatus.EXPIRED);
    }
}
