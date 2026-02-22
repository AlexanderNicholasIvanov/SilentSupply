package com.silentsupply.notification;

import com.silentsupply.notification.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Server-Sent Event (SSE) emitters for real-time notification delivery.
 * Each company has at most one active SSE connection.
 */
@Service
@Slf4j
public class SseEmitterService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Registers a new SSE emitter for a company. Replaces any existing connection.
     *
     * @param companyId the company's ID
     * @return the SSE emitter for the client to consume
     */
    public SseEmitter register(Long companyId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(companyId);
            log.debug("SSE emitter completed for company {}", companyId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(companyId);
            log.debug("SSE emitter timed out for company {}", companyId);
        });
        emitter.onError(e -> {
            emitters.remove(companyId);
            log.debug("SSE emitter error for company {}: {}", companyId, e.getMessage());
        });

        emitters.put(companyId, emitter);
        log.debug("SSE emitter registered for company {}", companyId);
        return emitter;
    }

    /**
     * Sends a notification to a company's SSE stream if they have an active connection.
     *
     * @param companyId    the recipient company's ID
     * @param notification the notification to send
     */
    public void send(Long companyId, NotificationResponse notification) {
        SseEmitter emitter = emitters.get(companyId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
                log.debug("Sent SSE notification to company {}: {}", companyId, notification.getType());
            } catch (IOException e) {
                emitters.remove(companyId);
                log.debug("Failed to send SSE to company {}, removing emitter", companyId);
            }
        }
    }
}
