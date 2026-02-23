package com.silentsupply.messaging.dto;

import com.silentsupply.messaging.ConversationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending a message. Supports two modes:
 * <ul>
 *   <li>Scoped: provide {@code referenceType} + {@code referenceId} for RFQ/ORDER conversations</li>
 *   <li>Direct: provide {@code recipientCompanyId} for direct company-to-company chat</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    /** Conversation ID if sending to an existing conversation. */
    private Long conversationId;

    /** Reference type for auto-creating scoped conversations (RFQ or ORDER). */
    private ConversationType referenceType;

    /** Reference entity ID (RFQ ID or Order ID) for scoped conversations. */
    private Long referenceId;

    /** Recipient company ID for direct conversations. */
    private Long recipientCompanyId;

    /** Optional subject line for the conversation. */
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;

    /** The message content. */
    @NotBlank(message = "Message content is required")
    @Size(max = 10000, message = "Message must not exceed 10000 characters")
    private String content;
}
