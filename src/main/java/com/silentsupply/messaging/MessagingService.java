package com.silentsupply.messaging;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.messaging.dto.ConversationResponse;
import com.silentsupply.messaging.dto.MessageResponse;
import com.silentsupply.messaging.dto.SendMessageRequest;
import com.silentsupply.order.CatalogOrder;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing conversations and messages between companies.
 * Supports direct company-to-company and scoped (RFQ/order) conversations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final CompanyRepository companyRepository;
    private final RfqRepository rfqRepository;
    private final CatalogOrderRepository orderRepository;

    /**
     * Sends a message, auto-creating the conversation if it doesn't exist.
     *
     * @param senderCompanyId the sender's company ID
     * @param request         the send message request
     * @return the created message response
     */
    @Transactional
    public MessageResponse sendMessage(Long senderCompanyId, SendMessageRequest request) {
        Company sender = companyRepository.findById(senderCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", senderCompanyId));

        Conversation conversation = resolveConversation(sender, request);

        if (!participantRepository.existsByConversationIdAndCompanyId(conversation.getId(), senderCompanyId)) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .senderCompany(sender)
                .content(request.getContent())
                .build();

        Message saved = messageRepository.save(message);

        // Update conversation timestamp to keep ordering fresh
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        log.debug("Message sent by company {} in conversation {}", senderCompanyId, conversation.getId());
        return messageMapper.toResponse(saved);
    }

    /**
     * Lists all conversations for a company with summary info.
     *
     * @param companyId the company's ID
     * @return list of conversation summaries
     */
    public List<ConversationResponse> getConversations(Long companyId) {
        List<Conversation> conversations = conversationRepository.findByParticipantCompanyId(companyId);
        return conversations.stream()
                .map(conv -> toConversationResponse(conv, companyId))
                .toList();
    }

    /**
     * Gets paginated messages for a conversation after verifying participant access.
     *
     * @param conversationId the conversation ID
     * @param companyId      the requesting company's ID
     * @param pageable       pagination parameters
     * @return page of message responses
     */
    public Page<MessageResponse> getMessages(Long conversationId, Long companyId, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        if (!participantRepository.existsByConversationIdAndCompanyId(conversationId, companyId)) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(messageMapper::toResponse);
    }

    /**
     * Marks a conversation as read for a participant.
     *
     * @param conversationId the conversation ID
     * @param companyId      the company marking as read
     */
    @Transactional
    public void markAsRead(Long conversationId, Long companyId) {
        ConversationParticipant participant = participantRepository
                .findByConversationIdAndCompanyId(conversationId, companyId)
                .orElseThrow(() -> new AccessDeniedException("You are not a participant in this conversation"));

        participant.setLastReadAt(LocalDateTime.now());
        participantRepository.save(participant);
    }

    /**
     * Gets the total unread message count across all conversations for a company.
     *
     * @param companyId the company's ID
     * @return map containing the total unread count
     */
    public Map<String, Long> getUnreadCount(Long companyId) {
        List<Conversation> conversations = conversationRepository.findByParticipantCompanyId(companyId);
        long total = 0;

        for (Conversation conv : conversations) {
            ConversationParticipant participant = participantRepository
                    .findByConversationIdAndCompanyId(conv.getId(), companyId)
                    .orElse(null);
            if (participant != null) {
                total += countUnread(conv.getId(), participant.getLastReadAt());
            }
        }

        return Map.of("unreadCount", total);
    }

    /**
     * Resolves the target conversation from the request, creating one if necessary.
     */
    private Conversation resolveConversation(Company sender, SendMessageRequest request) {
        // Case 1: Existing conversation by ID
        if (request.getConversationId() != null) {
            return conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Conversation", "id", request.getConversationId()));
        }

        // Case 2: Scoped conversation (RFQ or ORDER)
        if (request.getReferenceType() != null && request.getReferenceId() != null
                && request.getReferenceType() != ConversationType.DIRECT) {
            return findOrCreateScopedConversation(sender, request.getReferenceType(), request.getReferenceId());
        }

        // Case 3: Direct conversation
        if (request.getRecipientCompanyId() != null) {
            return findOrCreateDirectConversation(sender, request.getRecipientCompanyId());
        }

        throw new BusinessRuleException(
                "Must provide conversationId, referenceType+referenceId, or recipientCompanyId");
    }

    /**
     * Finds or creates a scoped conversation for an RFQ or order.
     */
    private Conversation findOrCreateScopedConversation(Company sender, ConversationType type, Long referenceId) {
        return conversationRepository.findByTypeAndReferenceId(type, referenceId)
                .orElseGet(() -> createScopedConversation(sender, type, referenceId));
    }

    /**
     * Creates a new scoped conversation with participants from the referenced entity.
     */
    private Conversation createScopedConversation(Company sender, ConversationType type, Long referenceId) {
        Company otherParty;
        String subject;

        if (type == ConversationType.RFQ) {
            Rfq rfq = rfqRepository.findById(referenceId)
                    .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", referenceId));
            otherParty = rfq.getBuyer().getId().equals(sender.getId()) ? rfq.getSupplier() : rfq.getBuyer();
            subject = "RFQ #" + referenceId;
        } else if (type == ConversationType.ORDER) {
            CatalogOrder order = orderRepository.findById(referenceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", referenceId));
            otherParty = order.getBuyer().getId().equals(sender.getId()) ? order.getSupplier() : order.getBuyer();
            subject = "Order #" + referenceId;
        } else {
            throw new BusinessRuleException("Invalid reference type for scoped conversation: " + type);
        }

        Conversation conversation = Conversation.builder()
                .type(type)
                .referenceId(referenceId)
                .subject(subject)
                .build();
        Conversation saved = conversationRepository.save(conversation);

        addParticipant(saved, sender);
        addParticipant(saved, otherParty);

        return saved;
    }

    /**
     * Finds or creates a direct conversation between two companies.
     */
    private Conversation findOrCreateDirectConversation(Company sender, Long recipientId) {
        if (sender.getId().equals(recipientId)) {
            throw new BusinessRuleException("Cannot start a conversation with yourself");
        }

        Company recipient = companyRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", recipientId));

        return conversationRepository
                .findDirectConversation(ConversationType.DIRECT, sender.getId(), recipientId)
                .orElseGet(() -> {
                    Conversation conversation = Conversation.builder()
                            .type(ConversationType.DIRECT)
                            .subject(null)
                            .build();
                    Conversation saved = conversationRepository.save(conversation);
                    addParticipant(saved, sender);
                    addParticipant(saved, recipient);
                    return saved;
                });
    }

    /**
     * Adds a company as a participant to a conversation.
     */
    private void addParticipant(Conversation conversation, Company company) {
        ConversationParticipant participant = ConversationParticipant.builder()
                .conversation(conversation)
                .company(company)
                .build();
        participantRepository.save(participant);
    }

    /**
     * Converts a Conversation entity to a response DTO with summary fields.
     */
    private ConversationResponse toConversationResponse(Conversation conversation, Long requestingCompanyId) {
        Message lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());

        ConversationParticipant participant = participantRepository
                .findByConversationIdAndCompanyId(conversation.getId(), requestingCompanyId)
                .orElse(null);

        long unread = participant != null
                ? countUnread(conversation.getId(), participant.getLastReadAt())
                : 0;

        List<ConversationParticipant> participants = participantRepository.findByConversationId(conversation.getId());
        List<ConversationResponse.ParticipantInfo> participantInfos = participants.stream()
                .map(p -> ConversationResponse.ParticipantInfo.builder()
                        .companyId(p.getCompany().getId())
                        .companyName(p.getCompany().getName())
                        .build())
                .toList();

        return ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .referenceId(conversation.getReferenceId())
                .subject(conversation.getSubject())
                .participants(participantInfos)
                .lastMessagePreview(lastMessage != null ? truncate(lastMessage.getContent(), 100) : null)
                .lastMessageSenderName(lastMessage != null ? lastMessage.getSenderCompany().getName() : null)
                .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .unreadCount(unread)
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    /**
     * Counts unread messages for a participant. If lastReadAt is null, all messages are unread.
     */
    private long countUnread(Long conversationId, LocalDateTime lastReadAt) {
        if (lastReadAt == null) {
            return messageRepository.countByConversationId(conversationId);
        }
        return messageRepository.countByConversationIdAndCreatedAtAfter(conversationId, lastReadAt);
    }

    /**
     * Truncates a string to the given max length, appending "..." if truncated.
     */
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
