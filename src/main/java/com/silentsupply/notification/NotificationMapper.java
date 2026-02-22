package com.silentsupply.notification;

import com.silentsupply.notification.dto.NotificationResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link Notification} entities to response DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NotificationMapper {

    /**
     * Converts a Notification entity to a response DTO.
     *
     * @param notification the notification entity
     * @return the response DTO
     */
    @Mapping(source = "recipient.id", target = "recipientId")
    NotificationResponse toResponse(Notification notification);
}
