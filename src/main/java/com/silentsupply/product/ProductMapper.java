package com.silentsupply.product;

import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between {@link Product} entities and DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProductMapper {

    /**
     * Converts a product request DTO to a Product entity.
     * The supplier must be set separately by the service.
     *
     * @param request the product request
     * @return the Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequest request);

    /**
     * Converts a Product entity to a response DTO.
     *
     * @param product the product entity
     * @return the response DTO
     */
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    ProductResponse toResponse(Product product);

    /**
     * Updates an existing product entity with values from the request DTO.
     *
     * @param request the updated product details
     * @param product the existing product to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
