package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.services.AuthService;
import com.sep490.vtuber_fanhub.services.SseNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * Controller for SSE (Server-Sent Events) notifications
 * Provides endpoint for clients to subscribe to real-time notifications
 * 
 * Usage:
 * - Client connects to /vhub/api/v1/notifications/stream
 * - Server maintains the connection and sends events as they occur
 * - Events include: VTuber application results, post likes, post comments
 */
@RestController
@RequestMapping("vhub/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class SseNotificationController {

    private final SseNotificationService sseNotificationService;
    private final AuthService authService;

    /**
     * SSE endpoint for real-time notifications
     * Client should establish a persistent connection to this endpoint
     *
     * Example JavaScript client code:
     * const eventSource = new EventSource('/vhub/api/v1/notifications/stream', {
     *     withCredentials: true,
     *     headers: { 'Authorization': 'Bearer <token>' }
     * });
     *
     * eventSource.addEventListener('notification', (event) => {
     *     const notification = JSON.parse(event.data);
     *     console.log('New notification:', notification);
     * });
     *
     * @param request the HTTP request containing the JWT token in Authorization header
     * @return SseEmitter that maintains the connection and streams notifications
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(HttpServletRequest request) {
        User user = authService.getUserFromToken(request);

        log.info("User {} connecting to notification stream", user.getId());

        // Create and return SSE emitter for this user
        return sseNotificationService.createEmitter(user.getId());
    }

    /**
     * Health check endpoint for SSE notification service
     * Returns the count of active connections
     *
     * @return map with active connection count
     */
    @GetMapping("/status")
    public ResponseEntity<?> getNotificationStatus() {
        int activeConnections = sseNotificationService.getActiveEmitterCount();
        
        return ResponseEntity.ok()
                .body(Map.of(
                        "activeConnections", activeConnections,
                        "status", "operational"
                ));
    }
}
