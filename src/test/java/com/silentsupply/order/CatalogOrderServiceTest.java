package com.silentsupply.order;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
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
 * Unit tests for {@link CatalogOrderService}.
 */
@ExtendWith(MockitoExtension.class)
class CatalogOrderServiceTest {

    @Mock
    private CatalogOrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CatalogOrderMapper orderMapper;

    @InjectMocks
    private CatalogOrderService orderService;

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
    void placeOrder_withValidRequest_createsOrderAndDeductsStock() {
        OrderRequest request = OrderRequest.builder().productId(10L).quantity(5).build();
        OrderResponse expectedResponse = OrderResponse.builder()
                .id(100L).buyerId(2L).productId(10L).supplierId(1L)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.PLACED).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(CatalogOrder.class))).thenAnswer(invocation -> {
            CatalogOrder order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });
        when(orderMapper.toResponse(any(CatalogOrder.class))).thenReturn(expectedResponse);

        OrderResponse result = orderService.placeOrder(2L, request);

        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(product.getAvailableQuantity()).isEqualTo(95);
        verify(productRepository).save(product);
    }

    @Test
    void placeOrder_withInsufficientStock_throwsBusinessRuleException() {
        OrderRequest request = OrderRequest.builder().productId(10L).quantity(200).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_withDiscontinuedProduct_throwsBusinessRuleException() {
        product.setStatus(ProductStatus.DISCONTINUED);
        OrderRequest request = OrderRequest.builder().productId(10L).quantity(5).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void placeOrder_withNonExistentProduct_throwsResourceNotFoundException() {
        OrderRequest request = OrderRequest.builder().productId(99L).quantity(5).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_withValidTransition_updatesStatus() {
        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.PLACED).build();
        order.setId(100L);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(100L).status(OrderStatus.CONFIRMED).build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        OrderResponse result = orderService.updateStatus(100L, OrderStatus.CONFIRMED);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatus_withInvalidTransition_throwsBusinessRuleException() {
        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.PLACED).build();
        order.setId(100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(100L, OrderStatus.DELIVERED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_fromDelivered_throwsBusinessRuleException() {
        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.DELIVERED).build();
        order.setId(100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(100L, OrderStatus.CANCELLED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid status transition");
    }
}
