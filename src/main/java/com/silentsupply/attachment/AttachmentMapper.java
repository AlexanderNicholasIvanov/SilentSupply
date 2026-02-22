package com.silentsupply.attachment;

import com.silentsupply.attachment.dto.AttachmentResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link Attachment} entities to response DTOs.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AttachmentMapper {

    /**
     * Converts an Attachment entity to a response DTO.
     *
     * @param attachment the attachment entity
     * @return the response DTO
     */
    @Mapping(source = "uploader.id", target = "uploaderId")
    @Mapping(source = "uploader.name", target = "uploaderName")
    AttachmentResponse toResponse(Attachment attachment);
}
