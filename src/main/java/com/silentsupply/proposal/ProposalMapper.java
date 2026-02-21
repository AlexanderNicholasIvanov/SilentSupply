package com.silentsupply.proposal;

import com.silentsupply.proposal.dto.ProposalResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link Proposal} entities to response DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProposalMapper {

    /**
     * Converts a Proposal entity to a response DTO.
     *
     * @param proposal the proposal entity
     * @return the response DTO
     */
    @Mapping(source = "rfq.id", target = "rfqId")
    ProposalResponse toResponse(Proposal proposal);
}
