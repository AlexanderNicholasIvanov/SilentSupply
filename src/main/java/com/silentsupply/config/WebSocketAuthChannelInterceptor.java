package com.silentsupply.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * STOMP channel interceptor that authenticates WebSocket connections via JWT.
 * Extracts the JWT from the Authorization native header on CONNECT frames
 * and sets a {@link CompanyPrincipal} for user-destination routing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CompanyUserDetailsService userDetailsService;

    /**
     * Intercepts STOMP CONNECT frames to validate the JWT token and set the user principal.
     *
     * @param message the incoming message
     * @param channel the message channel
     * @return the message (possibly with updated headers)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    Long companyId = jwtTokenProvider.getCompanyIdFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    CompanyPrincipal principal = new CompanyPrincipal(companyId, email);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, userDetails.getAuthorities());

                    accessor.setUser(auth);
                    log.debug("WebSocket CONNECT authenticated for company {}", companyId);
                } else {
                    log.warn("WebSocket CONNECT with invalid JWT token");
                }
            } else {
                log.warn("WebSocket CONNECT without Authorization header");
            }
        }

        return message;
    }
}
