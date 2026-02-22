package com.silentsupply.attachment;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.company.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a file attachment associated with a product, order, or RFQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "attachments")
public class Attachment extends BaseEntity {

    /** Original file name as uploaded. */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /** MIME content type of the file. */
    @Column(name = "content_type", nullable = false)
    private String contentType;

    /** File size in bytes. */
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    /** Path to the file on the storage filesystem. */
    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    /** The type of entity this attachment belongs to. */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private AttachmentEntityType entityType;

    /** The ID of the entity this attachment belongs to. */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /** The company that uploaded this attachment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private Company uploader;
}
