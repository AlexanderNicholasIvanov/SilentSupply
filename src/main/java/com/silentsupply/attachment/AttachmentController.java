package com.silentsupply.attachment;

import com.silentsupply.attachment.dto.AttachmentResponse;
import com.silentsupply.config.CompanyUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for file attachment operations.
 * Supports upload, download, listing, and deletion of attachments.
 */
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "File attachment management for products, orders, and RFQs")
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * Uploads a file attachment for an entity.
     *
     * @param userDetails the authenticated user
     * @param file        the file to upload
     * @param entityType  the type of entity to attach to
     * @param entityId    the entity's ID
     * @return the created attachment with HTTP 201
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file attachment")
    public ResponseEntity<AttachmentResponse> upload(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") AttachmentEntityType entityType,
            @RequestParam("entityId") Long entityId) {
        AttachmentResponse response = attachmentService.upload(file, entityType, entityId, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Downloads a file attachment.
     *
     * @param id the attachment ID
     * @return the file content as a streaming response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Download a file attachment")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment attachment = attachmentService.getAttachment(id);
        Resource resource = attachmentService.download(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Lists all attachments for a given entity.
     *
     * @param entityType the type of entity
     * @param entityId   the entity's ID
     * @return list of attachments
     */
    @GetMapping
    @Operation(summary = "List attachments for an entity")
    public ResponseEntity<List<AttachmentResponse>> listByEntity(
            @RequestParam("entityType") AttachmentEntityType entityType,
            @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(attachmentService.listByEntity(entityType, entityId));
    }

    /**
     * Deletes an attachment. Only the uploader can delete their attachments.
     *
     * @param userDetails the authenticated user
     * @param id          the attachment ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file attachment (owner only)")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @PathVariable Long id) {
        attachmentService.delete(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
