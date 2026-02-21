package com.silentsupply.order;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.order.dto.OrderStatusUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for catalog order operations.
 * Buyers place orders; suppliers and buyers can view and track them.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Catalog order placement and management")
public class CatalogOrderController {

    private final CatalogOrderService orderService;

    /**
     * Places a new catalog order. Buyer-only.
     *
     * @param userDetails the authenticated buyer
     * @param request     the order details
     * @return the created order with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Place a new catalog order (buyer only)")
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the order details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    /**
     * Lists orders for the authenticated user (buyer or supplier).
     *
     * @param userDetails the authenticated user
     * @return list of orders
     */
    @GetMapping
    @Operation(summary = "List orders for authenticated user")
    public ResponseEntity<List<OrderResponse>> listOrders(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        List<OrderResponse> orders;
        if ("SUPPLIER".equals(userDetails.getRole())) {
            orders = orderService.listBySupplier(userDetails.getId());
        } else {
            orders = orderService.listByBuyer(userDetails.getId());
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * Updates the status of an order.
     *
     * @param id           the order ID
     * @param statusUpdate the new status
     * @return the updated order
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdate statusUpdate) {
        return ResponseEntity.ok(orderService.updateStatus(id, statusUpdate.getStatus()));
    }
}
