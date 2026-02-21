package com.silentsupply.negotiation;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for CRUD operations on negotiation rules.
 * Only the owning supplier can manage rules for their products.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NegotiationRuleService {

    private final NegotiationRuleRepository ruleRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final NegotiationRuleMapper ruleMapper;

    /**
     * Creates a new negotiation rule for a product.
     *
     * @param supplierId the supplier's company ID
     * @param request    the rule details
     * @return the created rule
     * @throws BusinessRuleException if a rule already exists for this supplier-product pair
     * @throws AccessDeniedException if the supplier does not own the product
     */
    @Transactional
    public NegotiationRuleResponse create(Long supplierId, NegotiationRuleRequest request) {
        Company supplier = companyRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", supplierId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.getSupplier().getId().equals(supplierId)) {
            throw new AccessDeniedException("You can only set rules for your own products");
        }

        if (ruleRepository.findBySupplierIdAndProductId(supplierId, request.getProductId()).isPresent()) {
            throw new BusinessRuleException("Negotiation rule already exists for this product");
        }

        if (request.getPriceFloor().compareTo(request.getAutoAcceptThreshold()) > 0) {
            throw new BusinessRuleException("Price floor must not exceed auto-accept threshold");
        }

        NegotiationRule rule = ruleMapper.toEntity(request);
        rule.setSupplier(supplier);
        rule.setProduct(product);

        NegotiationRule saved = ruleRepository.save(rule);
        return ruleMapper.toResponse(saved);
    }

    /**
     * Updates an existing negotiation rule.
     *
     * @param ruleId     the rule ID
     * @param supplierId the supplier's company ID
     * @param request    the updated rule details
     * @return the updated rule
     */
    @Transactional
    public NegotiationRuleResponse update(Long ruleId, Long supplierId, NegotiationRuleRequest request) {
        NegotiationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("NegotiationRule", "id", ruleId));

        if (!rule.getSupplier().getId().equals(supplierId)) {
            throw new AccessDeniedException("You can only manage your own negotiation rules");
        }

        if (request.getPriceFloor().compareTo(request.getAutoAcceptThreshold()) > 0) {
            throw new BusinessRuleException("Price floor must not exceed auto-accept threshold");
        }

        ruleMapper.updateEntity(request, rule);
        NegotiationRule saved = ruleRepository.save(rule);
        return ruleMapper.toResponse(saved);
    }

    /**
     * Deletes a negotiation rule.
     *
     * @param ruleId     the rule ID
     * @param supplierId the supplier's company ID
     */
    @Transactional
    public void delete(Long ruleId, Long supplierId) {
        NegotiationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("NegotiationRule", "id", ruleId));

        if (!rule.getSupplier().getId().equals(supplierId)) {
            throw new AccessDeniedException("You can only delete your own negotiation rules");
        }

        ruleRepository.delete(rule);
    }

    /**
     * Lists all negotiation rules for a supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of rules
     */
    public List<NegotiationRuleResponse> listBySupplier(Long supplierId) {
        return ruleRepository.findBySupplierId(supplierId).stream()
                .map(ruleMapper::toResponse)
                .toList();
    }
}
