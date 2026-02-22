package com.silentsupply.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link Attachment} entities.
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * Finds all attachments for a given entity.
     *
     * @param entityType the type of entity
     * @param entityId   the entity's ID
     * @return list of attachments
     */
    List<Attachment> findByEntityTypeAndEntityId(AttachmentEntityType entityType, Long entityId);
}
