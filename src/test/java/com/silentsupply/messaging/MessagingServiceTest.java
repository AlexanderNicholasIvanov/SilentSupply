package com.silentsupply.messaging;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.messaging.dto.ConversationResponse;
import com.silentsupply.messaging.dto.MessageResponse;
import com.silentsupply.messaging.dto.SendMessageRequest;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.rfq.RfqRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MessagingService}.
 */
@ExtendWith(MockitoExtension.class)
class MessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationParticipantRepository participantRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RfqRepository rfqRepository;

    @Mock
    private CatalogOrderRepository orderRepository;

    @InjectMocks
    private MessagingService messagingService;

    @Test
    void sendMessage_toExistingConversation_savesMessage() {
        Company sender = buildCompany(1L, "Sender Co");
        Conversation conversation = buildConversation(10L, ConversationType.DIRECT);
        MessageResponse expectedResponse = MessageResponse.builder()
                .id(100L).conversationId(10L).content("Hello").build();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(participantRepository.existsByConversationIdAndCompanyId(10L, 1L)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });
        when(conversationRepository.save(any())).thenReturn(conversation);
        when(messageMapper.toResponse(any())).thenReturn(expectedResponse);

        SendMessageRequest request = SendMessageRequest.builder()
                .conversationId(10L).content("Hello").build();

        MessageResponse result = messagingService.sendMessage(1L, request);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getContent()).isEqualTo("Hello");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessage_nonParticipant_throwsAccessDenied() {
        Company sender = buildCompany(1L, "Sender Co");
        Conversation conversation = buildConversation(10L, ConversationType.DIRECT);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(participantRepository.existsByConversationIdAndCompanyId(10L, 1L)).thenReturn(false);

        SendMessageRequest request = SendMessageRequest.builder()
                .conversationId(10L).content("Hello").build();

        assertThatThrownBy(() -> messagingService.sendMessage(1L, request))
                .isInstanceOf(AccessDeniedException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_directConversation_createsIfNotExists() {
        Company sender = buildCompany(1L, "Sender Co");
        Company recipient = buildCompany(2L, "Recipient Co");
        Conversation newConv = buildConversation(10L, ConversationType.DIRECT);
        MessageResponse expectedResponse = MessageResponse.builder()
                .id(100L).conversationId(10L).content("Hi there").build();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(companyRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(conversationRepository.findDirectConversation(ConversationType.DIRECT, 1L, 2L))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> {
            Conversation c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantRepository.existsByConversationIdAndCompanyId(10L, 1L)).thenReturn(true);
        when(messageRepository.save(any())).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });
        when(messageMapper.toResponse(any())).thenReturn(expectedResponse);

        SendMessageRequest request = SendMessageRequest.builder()
                .recipientCompanyId(2L).content("Hi there").build();

        MessageResponse result = messagingService.sendMessage(1L, request);

        assertThat(result.getConversationId()).isEqualTo(10L);
        ArgumentCaptor<ConversationParticipant> captor = ArgumentCaptor.forClass(ConversationParticipant.class);
        verify(participantRepository, org.mockito.Mockito.times(2)).save(captor.capture());
    }

    @Test
    void sendMessage_toSelf_throwsBusinessRule() {
        Company sender = buildCompany(1L, "Sender Co");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sender));

        SendMessageRequest request = SendMessageRequest.builder()
                .recipientCompanyId(1L).content("Hello me").build();

        assertThatThrownBy(() -> messagingService.sendMessage(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    void sendMessage_noTarget_throwsBusinessRule() {
        Company sender = buildCompany(1L, "Sender Co");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sender));

        SendMessageRequest request = SendMessageRequest.builder().content("Hello").build();

        assertThatThrownBy(() -> messagingService.sendMessage(1L, request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void getConversations_returnsConversationsForCompany() {
        Conversation conv = buildConversation(10L, ConversationType.DIRECT);
        conv.setParticipants(List.of(
                buildParticipant(conv, buildCompany(1L, "Company A")),
                buildParticipant(conv, buildCompany(2L, "Company B"))
        ));
        conv.setCreatedAt(LocalDateTime.now());

        when(conversationRepository.findByParticipantCompanyId(1L)).thenReturn(List.of(conv));
        when(participantRepository.findByConversationId(10L)).thenReturn(conv.getParticipants());
        when(participantRepository.findByConversationIdAndCompanyId(10L, 1L))
                .thenReturn(Optional.of(conv.getParticipants().get(0)));
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(10L)).thenReturn(null);
        when(messageRepository.countByConversationId(10L)).thenReturn(0L);

        List<ConversationResponse> result = messagingService.getConversations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getParticipants()).hasSize(2);
    }

    @Test
    void markAsRead_updatesLastReadAt() {
        ConversationParticipant participant = ConversationParticipant.builder().build();
        participant.setId(1L);

        when(participantRepository.findByConversationIdAndCompanyId(10L, 1L))
                .thenReturn(Optional.of(participant));
        when(participantRepository.save(any())).thenReturn(participant);

        messagingService.markAsRead(10L, 1L);

        assertThat(participant.getLastReadAt()).isNotNull();
        verify(participantRepository).save(participant);
    }

    @Test
    void markAsRead_nonParticipant_throwsAccessDenied() {
        when(participantRepository.findByConversationIdAndCompanyId(10L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> messagingService.markAsRead(10L, 99L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUnreadCount_aggregatesAcrossConversations() {
        Conversation conv1 = buildConversation(10L, ConversationType.DIRECT);
        Conversation conv2 = buildConversation(20L, ConversationType.RFQ);
        ConversationParticipant p1 = ConversationParticipant.builder().lastReadAt(null).build();
        ConversationParticipant p2 = ConversationParticipant.builder()
                .lastReadAt(LocalDateTime.now().minusHours(1)).build();

        when(conversationRepository.findByParticipantCompanyId(1L)).thenReturn(List.of(conv1, conv2));
        when(participantRepository.findByConversationIdAndCompanyId(10L, 1L))
                .thenReturn(Optional.of(p1));
        when(participantRepository.findByConversationIdAndCompanyId(20L, 1L))
                .thenReturn(Optional.of(p2));
        when(messageRepository.countByConversationId(10L)).thenReturn(3L);
        when(messageRepository.countByConversationIdAndCreatedAtAfter(eq(20L), any())).thenReturn(2L);

        Map<String, Long> result = messagingService.getUnreadCount(1L);

        assertThat(result.get("unreadCount")).isEqualTo(5L);
    }

    private Company buildCompany(Long id, String name) {
        Company company = Company.builder().name(name).build();
        company.setId(id);
        return company;
    }

    private Conversation buildConversation(Long id, ConversationType type) {
        Conversation conv = Conversation.builder().type(type).build();
        conv.setId(id);
        return conv;
    }

    private ConversationParticipant buildParticipant(Conversation conversation, Company company) {
        return ConversationParticipant.builder()
                .conversation(conversation)
                .company(company)
                .build();
    }
}
