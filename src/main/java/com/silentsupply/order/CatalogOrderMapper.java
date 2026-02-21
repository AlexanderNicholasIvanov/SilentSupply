package com.silentsupply.order;

import com.silentsupply.order.dto.OrderResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link CatalogOrder} entities to response DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CatalogOrderMapper {

    /**
     * Converts a CatalogOrder entity to a response DTO.
     *
     * @param order the catalog order entity
     * @return the response DTO
     */
    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(source = "buyer.name", target = "buyerName")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    OrderResponse toResponse(CatalogOrder order);
}
