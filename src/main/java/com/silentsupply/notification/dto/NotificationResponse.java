package com.silentsupply.notification.dto;

import com.silentsupply.notification.NotificationReferenceType;
import com.silentsupply.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    /** Notification ID. */
    private Long id;

    /** Recipient's company ID. */
    private Long recipientId;

    /** Type of notification event. */
    private NotificationType type;

    /** Human-readable message. */
    private String message;

    /** ID of the referenced entity. */
    private Long referenceId;

    /** Type of the referenced entity. */
    private NotificationReferenceType referenceType;

    /** Whether this notification has been read. */
    private boolean read;

    /** When the notification was created. */
    private LocalDateTime createdAt;
}
