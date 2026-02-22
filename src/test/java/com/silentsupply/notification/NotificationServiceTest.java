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
import com.silentsupply.product.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NotificationService}.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SseEmitterService sseEmitterService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void list_allNotifications_returnsMapped() {
        Notification n = buildNotification(1L, 10L);
        NotificationResponse response = NotificationResponse.builder().id(1L).build();

        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(n));
        when(notificationMapper.toResponse(n)).thenReturn(response);

        List<NotificationResponse> result = notificationService.list(10L, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void list_unreadOnly_filtersCorrectly() {
        when(notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(10L))
                .thenReturn(List.of());

        List<NotificationResponse> result = notificationService.list(10L, true);

        assertThat(result).isEmpty();
    }

    @Test
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByRecipientIdAndReadFalse(10L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(10L);

        assertThat(count).isEqualTo(5);
    }

    @Test
    void markAsRead_ownNotification_marksRead() {
        Notification n = buildNotification(1L, 10L);
        NotificationResponse response = NotificationResponse.builder().id(1L).read(true).build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenReturn(n);
        when(notificationMapper.toResponse(n)).thenReturn(response);

        NotificationResponse result = notificationService.markAsRead(1L, 10L);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    void markAsRead_otherCompanyNotification_throwsAccessDenied() {
        Notification n = buildNotification(1L, 10L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 99L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void markAsRead_notFound_throwsResourceNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void notifyOrderStatusChange_createsTwoNotifications() {
        Company buyer = Company.builder().name("Buyer").build();
        buyer.setId(1L);
        Company supplier = Company.builder().name("Supplier").build();
        supplier.setId(2L);

        CatalogOrder order = CatalogOrder.builder().buyer(buyer).supplier(supplier).build();
        order.setId(100L);

        when(notificationRepository.save(any())).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(notificationMapper.toResponse(any())).thenReturn(NotificationResponse.builder().build());

        notificationService.notifyOrderStatusChange(order, OrderStatus.CONFIRMED);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        List<Notification> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getType()).isEqualTo(NotificationType.ORDER_STATUS_CHANGED);
    }

    @Test
    void notifyRfqSubmitted_notifiesSupplier() {
        Company supplier = Company.builder().name("Supplier").build();
        supplier.setId(2L);
        Product product = Product.builder().name("Widget").supplier(supplier).build();
        product.setId(10L);
        Rfq rfq = Rfq.builder().supplier(supplier).product(product).desiredQuantity(100).build();
        rfq.setId(50L);

        when(notificationRepository.save(any())).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(notificationMapper.toResponse(any())).thenReturn(NotificationResponse.builder().build());

        notificationService.notifyRfqSubmitted(rfq);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.RFQ_SUBMITTED);
        assertThat(captor.getValue().getRecipient().getId()).isEqualTo(2L);
    }

    private Notification buildNotification(Long id, Long recipientId) {
        Company recipient = Company.builder().name("Test").build();
        recipient.setId(recipientId);
        Notification n = Notification.builder()
                .recipient(recipient)
                .type(NotificationType.ORDER_STATUS_CHANGED)
                .message("Test notification")
                .build();
        n.setId(id);
        return n;
    }
}
