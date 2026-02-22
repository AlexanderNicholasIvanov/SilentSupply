package com.silentsupply.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a single message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    /** Message ID. */
    private Long id;

    /** Conversation ID this message belongs to. */
    private Long conversationId;

    /** Sender's company ID. */
    private Long senderCompanyId;

    /** Sender's company name. */
    private String senderCompanyName;

    /** The message content. */
    private String content;

    /** When the message was created. */
    private LocalDateTime createdAt;
}
