package com.silentsupply.negotiation;

import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between {@link NegotiationRule} entities and DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NegotiationRuleMapper {

    /**
     * Converts a NegotiationRule entity to a response DTO.
     *
     * @param rule the negotiation rule entity
     * @return the response DTO
     */
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    NegotiationRuleResponse toResponse(NegotiationRule rule);

    /**
     * Converts a request DTO to a NegotiationRule entity.
     *
     * @param request the rule request
     * @return the entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NegotiationRule toEntity(NegotiationRuleRequest request);

    /**
     * Updates an existing NegotiationRule entity from a request DTO.
     *
     * @param request the updated values
     * @param rule    the existing entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(NegotiationRuleRequest request, @MappingTarget NegotiationRule rule);
}
