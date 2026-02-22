package com.silentsupply.notification;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * REST controller for notification management and SSE streaming.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management and real-time streaming")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    /**
     * Lists notifications for the authenticated user.
     *
     * @param userDetails the authenticated user
     * @param unreadOnly  optional filter for unread only (defaults to false)
     * @return list of notifications
     */
    @GetMapping
    @Operation(summary = "List notifications (optionally unread only)")
    public ResponseEntity<List<NotificationResponse>> list(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.list(userDetails.getId(), unreadOnly));
    }

    /**
     * Returns the count of unread notifications.
     *
     * @param userDetails the authenticated user
     * @return unread count as JSON
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Marks a single notification as read.
     *
     * @param userDetails the authenticated user
     * @param id          the notification ID
     * @return the updated notification
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userDetails.getId()));
    }

    /**
     * Marks all notifications as read for the authenticated user.
     *
     * @param userDetails the authenticated user
     * @return HTTP 204 No Content
     */
    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Opens an SSE stream for real-time notifications.
     *
     * @param userDetails the authenticated user
     * @return the SSE emitter
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time notification stream (SSE)")
    public SseEmitter stream(@AuthenticationPrincipal CompanyUserDetails userDetails) {
        return sseEmitterService.register(userDetails.getId());
    }
}
