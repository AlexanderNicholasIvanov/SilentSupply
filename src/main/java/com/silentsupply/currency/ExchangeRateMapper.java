package com.silentsupply.currency;

import com.silentsupply.currency.dto.ExchangeRateResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for exchange rate entity/DTO conversion.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ExchangeRateMapper {

    /**
     * Converts an exchange rate entity to its response DTO.
     *
     * @param rate the entity
     * @return the response DTO
     */
    ExchangeRateResponse toResponse(ExchangeRate rate);
}
