package com.silentsupply.rfq;

import com.silentsupply.rfq.dto.RfqResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link Rfq} entities to response DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface RfqMapper {

    /**
     * Converts an RFQ entity to a response DTO.
     *
     * @param rfq the RFQ entity
     * @return the response DTO
     */
    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(source = "buyer.name", target = "buyerName")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    RfqResponse toResponse(Rfq rfq);
}
