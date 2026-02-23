package com.silentsupply.messaging;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.messaging.dto.ConversationResponse;
import com.silentsupply.messaging.dto.MessageResponse;
import com.silentsupply.messaging.dto.SendMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the messaging system.
 * Provides endpoints for conversations, messages, and read-state management.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Company-to-company messaging")
public class MessagingController {

    private final MessagingService messagingService;

    /**
     * Lists all conversations for the authenticated company.
     *
     * @param userDetails the authenticated user
     * @return list of conversation summaries
     */
    @GetMapping("/conversations")
    @Operation(summary = "List conversations for the authenticated company")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        return ResponseEntity.ok(messagingService.getConversations(userDetails.getId()));
    }

    /**
     * Gets paginated messages for a conversation.
     *
     * @param userDetails the authenticated user
     * @param id          the conversation ID
     * @param pageable    pagination parameters
     * @return page of messages
     */
    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get messages in a conversation (paginated)")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @PathVariable Long id,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(messagingService.getMessages(id, userDetails.getId(), pageable));
    }

    /**
     * Sends a message, auto-creating the conversation if needed.
     *
     * @param userDetails the authenticated user
     * @param request     the send message request
     * @return the created message
     */
    @PostMapping
    @Operation(summary = "Send a message (auto-creates conversation if needed)")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messagingService.sendMessage(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a single conversation's details.
     *
     * @param userDetails the authenticated user
     * @param id          the conversation ID
     * @return the conversation details
     */
    @GetMapping("/conversations/{id}/details")
    @Operation(summary = "Get conversation details")
    public ResponseEntity<ConversationResponse> getConversationDetails(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(messagingService.getConversationDetails(id, userDetails.getId()));
    }

    /**
     * Updates the subject of a conversation.
     *
     * @param userDetails the authenticated user
     * @param id          the conversation ID
     * @param body        map containing the new "subject" value
     * @return the updated conversation
     */
    @PatchMapping("/conversations/{id}/subject")
    @Operation(summary = "Update conversation subject")
    public ResponseEntity<ConversationResponse> updateSubject(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String subject = body.get("subject");
        return ResponseEntity.ok(messagingService.updateSubject(id, userDetails.getId(), subject));
    }

    /**
     * Marks a conversation as read for the authenticated company.
     *
     * @param userDetails the authenticated user
     * @param id          the conversation ID
     * @return HTTP 204 No Content
     */
    @PatchMapping("/conversations/{id}/read")
    @Operation(summary = "Mark a conversation as read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @PathVariable Long id) {
        messagingService.markAsRead(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets the total unread message count across all conversations.
     *
     * @param userDetails the authenticated user
     * @return unread count as JSON
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get total unread message count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        return ResponseEntity.ok(messagingService.getUnreadCount(userDetails.getId()));
    }
}
