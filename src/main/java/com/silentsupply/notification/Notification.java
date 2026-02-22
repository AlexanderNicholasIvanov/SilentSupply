package com.silentsupply.notification;

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
 * Represents a notification sent to a company about an event in the marketplace.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    /** The company receiving this notification. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Company recipient;

    /** The type of event that triggered this notification. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /** Human-readable notification message. */
    @Column(nullable = false)
    private String message;

    /** ID of the referenced entity (order, RFQ, or proposal). */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Type of the referenced entity. */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private NotificationReferenceType referenceType;

    /** Whether this notification has been read. */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;
}
