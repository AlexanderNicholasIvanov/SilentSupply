package com.silentsupply.notification;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.notification.dto.NotificationResponse;
import com.silentsupply.order.CatalogOrder;
import com.silentsupply.order.OrderStatus;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for creating, listing, and managing notifications.
 * Provides helper methods for each event type that other services call.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CompanyRepository companyRepository;
    private final SseEmitterService sseEmitterService;

    /**
     * Lists notifications for a company, optionally filtered to unread only.
     *
     * @param recipientId the recipient's company ID
     * @param unreadOnly  whether to return only unread notifications
     * @return list of notifications
     */
    public List<NotificationResponse> list(Long recipientId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(recipientId)
                : notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    /**
     * Returns the count of unread notifications for a company.
     *
     * @param recipientId the recipient's company ID
     * @return unread count
     */
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    /**
     * Marks a single notification as read.
     *
     * @param notificationId the notification ID
     * @param recipientId    the requesting company's ID (for ownership check)
     * @return the updated notification
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new AccessDeniedException("Cannot mark another company's notification as read");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    /**
     * Marks all notifications as read for a company.
     *
     * @param recipientId the company's ID
     */
    @Transactional
    public void markAllAsRead(Long recipientId) {
        List<Notification> unread = notificationRepository
                .findByRecipientIdAndReadFalseOrderByCreatedAtDesc(recipientId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /**
     * Notifies both buyer and supplier when an order status changes.
     *
     * @param order     the order whose status changed
     * @param newStatus the new status
     */
    @Transactional
    public void notifyOrderStatusChange(CatalogOrder order, OrderStatus newStatus) {
        String message = String.format("Order #%d status changed to %s", order.getId(), newStatus);

        createAndSend(order.getBuyer(), NotificationType.ORDER_STATUS_CHANGED,
                message, order.getId(), NotificationReferenceType.ORDER);
        createAndSend(order.getSupplier(), NotificationType.ORDER_STATUS_CHANGED,
                message, order.getId(), NotificationReferenceType.ORDER);
    }

    /**
     * Notifies the supplier when a new RFQ is submitted.
     *
     * @param rfq the submitted RFQ
     */
    @Transactional
    public void notifyRfqSubmitted(Rfq rfq) {
        String message = String.format("New RFQ #%d received for %s (qty: %d)",
                rfq.getId(), rfq.getProduct().getName(), rfq.getDesiredQuantity());

        createAndSend(rfq.getSupplier(), NotificationType.RFQ_SUBMITTED,
                message, rfq.getId(), NotificationReferenceType.RFQ);
    }

    /**
     * Notifies the supplier that a new proposal has been received for an RFQ.
     *
     * @param rfq the RFQ that received a proposal
     */
    @Transactional
    public void notifyProposalReceived(Rfq rfq) {
        String message = String.format("New proposal received for RFQ #%d (round %d)",
                rfq.getId(), rfq.getCurrentRound());

        createAndSend(rfq.getSupplier(), NotificationType.PROPOSAL_RECEIVED,
                message, rfq.getId(), NotificationReferenceType.RFQ);
    }

    /**
     * Notifies the buyer when a negotiation is resolved (accepted or rejected).
     *
     * @param rfq    the resolved RFQ
     * @param status the resolution status
     */
    @Transactional
    public void notifyNegotiationResolved(Rfq rfq, RfqStatus status) {
        String message = String.format("RFQ #%d negotiation resolved: %s", rfq.getId(), status);

        createAndSend(rfq.getBuyer(), NotificationType.NEGOTIATION_RESOLVED,
                message, rfq.getId(), NotificationReferenceType.RFQ);
    }

    /**
     * Creates a notification, saves it, and pushes it via SSE.
     */
    private void createAndSend(Company recipient, NotificationType type,
                                String message, Long referenceId,
                                NotificationReferenceType referenceType) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);
        sseEmitterService.send(recipient.getId(), response);
        log.debug("Notification sent to company {}: {}", recipient.getId(), type);
    }
}
