package com.silentsupply.company;

import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link Company} entities and DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CompanyMapper {

    /**
     * Converts a registration request DTO to a Company entity.
     * The password field is mapped but must be encoded by the service before persisting.
     *
     * @param request the registration request
     * @return the Company entity (password still in plain text)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Company toEntity(CompanyRequest request);

    /**
     * Converts a Company entity to a response DTO. Excludes the password.
     *
     * @param company the company entity
     * @return the response DTO
     */
    CompanyResponse toResponse(Company company);
}
