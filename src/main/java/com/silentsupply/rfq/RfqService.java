package com.silentsupply.rfq;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for RFQ lifecycle management.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RfqService {

    private static final int DEFAULT_EXPIRY_DAYS = 7;

    private final RfqRepository rfqRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final RfqMapper rfqMapper;

    /**
     * Submits a new RFQ for a product. Only buyers can submit RFQs.
     *
     * @param buyerId the buyer's company ID
     * @param request the RFQ details
     * @return the created RFQ
     * @throws BusinessRuleException if the product is not active
     */
    @Transactional
    public RfqResponse submit(Long buyerId, RfqRequest request) {
        Company buyer = companyRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", buyerId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot submit RFQ for inactive product");
        }

        Rfq rfq = Rfq.builder()
                .buyer(buyer)
                .product(product)
                .supplier(product.getSupplier())
                .desiredQuantity(request.getDesiredQuantity())
                .targetPrice(request.getTargetPrice())
                .deliveryDeadline(request.getDeliveryDeadline())
                .notes(request.getNotes())
                .status(RfqStatus.SUBMITTED)
                .currentRound(0)
                .maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(DEFAULT_EXPIRY_DAYS))
                .build();

        Rfq saved = rfqRepository.save(rfq);
        return rfqMapper.toResponse(saved);
    }

    /**
     * Retrieves an RFQ by its ID.
     *
     * @param id the RFQ ID
     * @return the RFQ details
     */
    public RfqResponse getById(Long id) {
        Rfq rfq = findRfqOrThrow(id);
        return rfqMapper.toResponse(rfq);
    }

    /**
     * Lists all RFQs for a specific buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of RFQs
     */
    public List<RfqResponse> listByBuyer(Long buyerId) {
        return rfqRepository.findByBuyerId(buyerId).stream()
                .map(rfqMapper::toResponse)
                .toList();
    }

    /**
     * Lists all RFQs for a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of RFQs
     */
    public List<RfqResponse> listBySupplier(Long supplierId) {
        return rfqRepository.findBySupplierId(supplierId).stream()
                .map(rfqMapper::toResponse)
                .toList();
    }

    /**
     * Expires all RFQs that have passed their expiration date and are still in an active status.
     *
     * @return the number of RFQs expired
     */
    @Transactional
    public int expireOverdueRfqs() {
        List<RfqStatus> activeStatuses = List.of(
                RfqStatus.SUBMITTED, RfqStatus.UNDER_REVIEW, RfqStatus.COUNTERED);
        List<Rfq> expired = rfqRepository.findByExpiresAtBeforeAndStatusIn(
                LocalDateTime.now(), activeStatuses);

        expired.forEach(rfq -> rfq.setStatus(RfqStatus.EXPIRED));
        rfqRepository.saveAll(expired);
        return expired.size();
    }

    /**
     * Finds an RFQ by ID or throws ResourceNotFoundException.
     *
     * @param id the RFQ ID
     * @return the RFQ entity
     */
    Rfq findRfqOrThrow(Long id) {
        return rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", id));
    }
}
