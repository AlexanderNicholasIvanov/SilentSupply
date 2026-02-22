package com.silentsupply.messaging;

import com.silentsupply.config.CompanyPrincipal;
import com.silentsupply.messaging.dto.MessageResponse;
import com.silentsupply.messaging.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP controller for real-time message delivery.
 * Persists messages via {@link MessagingService} and broadcasts to all conversation participants.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StompMessagingController {

    private final MessagingService messagingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationParticipantRepository participantRepository;

    /**
     * Handles messages sent to /app/chat.send via STOMP.
     * Persists the message and broadcasts it to all conversation participants
     * via their individual /user/queue/messages subscriptions.
     *
     * @param request   the message payload
     * @param principal the authenticated user (set by WebSocketAuthChannelInterceptor)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        CompanyPrincipal companyPrincipal = extractCompanyPrincipal(principal);
        Long senderCompanyId = companyPrincipal.getCompanyId();

        MessageResponse response = messagingService.sendMessage(senderCompanyId, request);

        // Broadcast to all participants in the conversation
        participantRepository.findByConversationId(response.getConversationId())
                .forEach(participant -> {
                    String destination = "/queue/messages";
                    messagingTemplate.convertAndSendToUser(
                            participant.getCompany().getId().toString(),
                            destination,
                            response);
                });

        log.debug("Message {} broadcast to conversation {} participants",
                response.getId(), response.getConversationId());
    }

    /**
     * Extracts the CompanyPrincipal from the STOMP authentication principal.
     */
    private CompanyPrincipal extractCompanyPrincipal(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
            return (CompanyPrincipal) authToken.getPrincipal();
        }
        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }
}
