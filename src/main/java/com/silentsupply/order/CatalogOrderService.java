package com.silentsupply.order;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.notification.NotificationService;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service layer for catalog order operations.
 * Handles order placement with stock validation, status transitions, and listing.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogOrderService {

    /** Defines the valid status transitions for catalog orders. */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PLACED, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    private final CatalogOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final CatalogOrderMapper orderMapper;
    private final NotificationService notificationService;

    /**
     * Places a new catalog order. Validates stock availability and deducts quantity.
     *
     * @param buyerId the buyer's company ID
     * @param request the order request
     * @return the created order
     * @throws BusinessRuleException if the product is not active or has insufficient stock
     */
    @Transactional
    public OrderResponse placeOrder(Long buyerId, OrderRequest request) {
        Company buyer = companyRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", buyerId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException("Product is not available for purchase: " + product.getStatus());
        }

        if (product.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessRuleException(
                    "Insufficient stock. Available: " + product.getAvailableQuantity()
                    + ", requested: " + request.getQuantity());
        }

        product.setAvailableQuantity(product.getAvailableQuantity() - request.getQuantity());
        productRepository.save(product);

        BigDecimal totalPrice = product.getBasePrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer)
                .product(product)
                .supplier(product.getSupplier())
                .quantity(request.getQuantity())
                .unitPrice(product.getBasePrice())
                .totalPrice(totalPrice)
                .status(OrderStatus.PLACED)
                .build();

        CatalogOrder saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the order details
     */
    public OrderResponse getById(Long id) {
        CatalogOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return orderMapper.toResponse(order);
    }

    /**
     * Lists all orders for a specific buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of orders
     */
    public List<OrderResponse> listByBuyer(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    /**
     * Lists all orders for a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of orders
     */
    public List<OrderResponse> listBySupplier(Long supplierId) {
        return orderRepository.findBySupplierId(supplierId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    /**
     * Transitions an order to a new status. Validates the transition is allowed.
     *
     * @param orderId   the order ID
     * @param newStatus the target status
     * @return the updated order
     * @throws BusinessRuleException if the status transition is not valid
     */
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        CatalogOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessRuleException(
                    "Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        CatalogOrder saved = orderRepository.save(order);
        notificationService.notifyOrderStatusChange(saved, newStatus);
        return orderMapper.toResponse(saved);
    }
}
