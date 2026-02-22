package com.silentsupply.messaging;

import com.silentsupply.messaging.dto.MessageResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link Message} entities to DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface MessageMapper {

    /**
     * Maps a Message entity to a MessageResponse DTO.
     *
     * @param message the message entity
     * @return the response DTO
     */
    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(source = "senderCompany.id", target = "senderCompanyId")
    @Mapping(source = "senderCompany.name", target = "senderCompanyName")
    MessageResponse toResponse(Message message);
}
