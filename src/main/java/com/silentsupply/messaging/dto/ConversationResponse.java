package com.silentsupply.messaging.dto;

import com.silentsupply.messaging.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a conversation summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    /** Conversation ID. */
    private Long id;

    /** Conversation type (DIRECT, RFQ, ORDER). */
    private ConversationType type;

    /** Reference entity ID for scoped conversations. */
    private Long referenceId;

    /** Conversation subject. */
    private String subject;

    /** List of participant company info. */
    private List<ParticipantInfo> participants;

    /** Preview of the last message. */
    private String lastMessagePreview;

    /** Sender company name of the last message. */
    private String lastMessageSenderName;

    /** Timestamp of the last message. */
    private LocalDateTime lastMessageAt;

    /** Number of unread messages for the requesting user. */
    private long unreadCount;

    /** When the conversation was created. */
    private LocalDateTime createdAt;

    /**
     * Summary info about a conversation participant.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {

        /** Company ID. */
        private Long companyId;

        /** Company name. */
        private String companyName;
    }
}
