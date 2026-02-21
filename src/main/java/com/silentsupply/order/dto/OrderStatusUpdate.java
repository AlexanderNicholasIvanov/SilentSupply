package com.silentsupply.order.dto;

import com.silentsupply.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an order's status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdate {

    /** The new status to transition to. */
    @NotNull(message = "Status is required")
    private OrderStatus status;
}
