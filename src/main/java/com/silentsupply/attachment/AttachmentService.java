package com.silentsupply.attachment;

import com.silentsupply.attachment.dto.AttachmentResponse;
import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.order.CatalogOrder;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service for managing file attachments with ownership validation.
 * Enforces that only authorized companies can upload/delete attachments for an entity.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final AttachmentMapper attachmentMapper;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final CatalogOrderRepository orderRepository;
    private final RfqRepository rfqRepository;

    /**
     * Uploads a file attachment for an entity after validating ownership.
     *
     * @param file       the file to upload
     * @param entityType the type of entity to attach to
     * @param entityId   the entity's ID
     * @param uploaderId the uploader's company ID
     * @return the created attachment
     */
    @Transactional
    public AttachmentResponse upload(MultipartFile file, AttachmentEntityType entityType,
                                     Long entityId, Long uploaderId) {
        validateOwnership(entityType, entityId, uploaderId);

        Company uploader = companyRepository.findById(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", uploaderId));

        String storagePath = fileStorageService.store(file);

        Attachment attachment = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(storagePath)
                .entityType(entityType)
                .entityId(entityId)
                .uploader(uploader)
                .build();

        Attachment saved = attachmentRepository.save(attachment);
        return attachmentMapper.toResponse(saved);
    }

    /**
     * Downloads a file attachment.
     *
     * @param id the attachment ID
     * @return the file as a Resource
     */
    public Resource download(Long id) {
        Attachment attachment = findOrThrow(id);
        return fileStorageService.load(attachment.getStoragePath());
    }

    /**
     * Returns the attachment metadata (needed for content type in download response).
     *
     * @param id the attachment ID
     * @return the attachment entity
     */
    public Attachment getAttachment(Long id) {
        return findOrThrow(id);
    }

    /**
     * Lists all attachments for a given entity.
     *
     * @param entityType the type of entity
     * @param entityId   the entity's ID
     * @return list of attachment responses
     */
    public List<AttachmentResponse> listByEntity(AttachmentEntityType entityType, Long entityId) {
        return attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(attachmentMapper::toResponse)
                .toList();
    }

    /**
     * Deletes an attachment. Only the uploader can delete their attachments.
     *
     * @param id         the attachment ID
     * @param uploaderId the requesting company's ID
     */
    @Transactional
    public void delete(Long id, Long uploaderId) {
        Attachment attachment = findOrThrow(id);

        if (!attachment.getUploader().getId().equals(uploaderId)) {
            throw new AccessDeniedException("Only the uploader can delete this attachment");
        }

        fileStorageService.delete(attachment.getStoragePath());
        attachmentRepository.delete(attachment);
    }

    /**
     * Validates that the uploader has permission to attach files to the given entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @param uploaderId the uploader's company ID
     */
    private void validateOwnership(AttachmentEntityType entityType, Long entityId, Long uploaderId) {
        switch (entityType) {
            case PRODUCT -> {
                Product product = productRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", entityId));
                if (!product.getSupplier().getId().equals(uploaderId)) {
                    throw new AccessDeniedException("Only the product's supplier can upload attachments");
                }
            }
            case ORDER -> {
                CatalogOrder order = orderRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Order", "id", entityId));
                if (!order.getBuyer().getId().equals(uploaderId)
                        && !order.getSupplier().getId().equals(uploaderId)) {
                    throw new AccessDeniedException("Only the order's buyer or supplier can upload attachments");
                }
            }
            case RFQ -> {
                Rfq rfq = rfqRepository.findById(entityId)
                        .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", entityId));
                if (!rfq.getBuyer().getId().equals(uploaderId)
                        && !rfq.getSupplier().getId().equals(uploaderId)) {
                    throw new AccessDeniedException("Only the RFQ's buyer or supplier can upload attachments");
                }
            }
        }
    }

    /**
     * Finds an attachment by ID or throws ResourceNotFoundException.
     *
     * @param id the attachment ID
     * @return the attachment entity
     */
    private Attachment findOrThrow(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));
    }
}
