package com.silentsupply.attachment.dto;

import com.silentsupply.attachment.AttachmentEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a file attachment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    /** Attachment ID. */
    private Long id;

    /** Original file name. */
    private String fileName;

    /** MIME content type. */
    private String contentType;

    /** File size in bytes. */
    private long fileSize;

    /** Type of entity this attachment belongs to. */
    private AttachmentEntityType entityType;

    /** ID of the entity this attachment belongs to. */
    private Long entityId;

    /** ID of the company that uploaded this file. */
    private Long uploaderId;

    /** Name of the company that uploaded this file. */
    private String uploaderName;

    /** When the attachment was created. */
    private LocalDateTime createdAt;
}
