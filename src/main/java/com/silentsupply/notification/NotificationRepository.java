package com.silentsupply.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link Notification} entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds all notifications for a recipient, ordered by most recent first.
     *
     * @param recipientId the recipient's company ID
     * @return list of notifications
     */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    /**
     * Finds unread notifications for a recipient, ordered by most recent first.
     *
     * @param recipientId the recipient's company ID
     * @return list of unread notifications
     */
    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(Long recipientId);

    /**
     * Counts unread notifications for a recipient.
     *
     * @param recipientId the recipient's company ID
     * @return count of unread notifications
     */
    long countByRecipientIdAndReadFalse(Long recipientId);
}
