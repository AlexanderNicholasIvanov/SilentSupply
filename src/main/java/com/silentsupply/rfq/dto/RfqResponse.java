package com.silentsupply.rfq.dto;

import com.silentsupply.rfq.RfqStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing an RFQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfqResponse {

    /** RFQ ID. */
    private Long id;

    /** Buyer's company ID. */
    private Long buyerId;

    /** Buyer's company name. */
    private String buyerName;

    /** Product ID. */
    private Long productId;

    /** Product name. */
    private String productName;

    /** Supplier's company ID. */
    private Long supplierId;

    /** Supplier's company name. */
    private String supplierName;

    /** Desired quantity. */
    private int desiredQuantity;

    /** Target price per unit. */
    private BigDecimal targetPrice;

    /** Required delivery deadline. */
    private LocalDate deliveryDeadline;

    /** Optional notes. */
    private String notes;

    /** Current status. */
    private RfqStatus status;

    /** Current negotiation round. */
    private int currentRound;

    /** Maximum negotiation rounds. */
    private int maxRounds;

    /** Expiration timestamp. */
    private LocalDateTime expiresAt;

    /** When the RFQ was created. */
    private LocalDateTime createdAt;
}
