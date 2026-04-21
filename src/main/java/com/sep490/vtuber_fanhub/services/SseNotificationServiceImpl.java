package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.configurations.SseConfig;
import com.sep490.vtuber_fanhub.dto.responses.NotificationEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of SSE notification service
 * Manages SSE connections for real-time user notifications
 * 
 * Features:
 * - Maintains one SSE connection per user
 * - Automatically cleans up on timeout/disconnect
 * - Thread-safe emitter management using ConcurrentHashMap
 * - Sends notifications for: VTuber application results, post likes, post comments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SseNotificationServiceImpl implements SseNotificationService {

    private final SseConfig.SseTimeoutConfig sseTimeoutConfig;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final Map<Long, Boolean> completedEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createEmitter(Long userId) {
        log.info("Creating SSE emitter for user: {}", userId);

        // Remove existing emitter if present (cleanup old connection)
        removeEmitter(userId);

        // Create new emitter with configured timeout
        SseEmitter emitter = new SseEmitter(sseTimeoutConfig.getTimeout());

        // Store the emitter
        emitters.put(userId, emitter);
        completedEmitters.put(userId, false);

        // Set up completion callback to clean up resources
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for user: {}", userId);
            completedEmitters.put(userId, true);
            emitters.remove(userId);
        });

        // Set up timeout callback
        emitter.onTimeout(() -> {
            log.warn("SSE connection timed out for user: {}", userId);
            completedEmitters.put(userId, true);
            emitters.remove(userId);
            try {
                emitter.complete();
            } catch (IllegalStateException e) {
                // Emitter already completed, ignore
            }
        });

        // Set up error callback
        emitter.onError(throwable -> {
            log.error("SSE connection error for user: {}: {}", userId, throwable.getMessage());
            completedEmitters.put(userId, true);
            emitters.remove(userId);
        });

        // Send initial connection event
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("connection")
                    .data(Map.of(
                            "type", "CONNECTED",
                            "userId", userId,
                            "timestamp", Instant.now().toString()
                    ));
            emitter.send(event);
            log.info("Initial connection event sent to user: {}", userId);
        } catch (IOException e) {
            log.error("Failed to send initial connection event to user: {}", userId, e);
            removeEmitter(userId);
        }

        return emitter;
    }

    @Override
    public void removeEmitter(Long userId) {
        SseEmitter existingEmitter = emitters.remove(userId);
        if (existingEmitter != null) {
            completedEmitters.put(userId, true);
            try {
                existingEmitter.complete();
            } catch (IllegalStateException e) {
            }
            log.info("Removed SSE emitter for user: {}", userId);
        }
    }

    @Override
    public void sendNotification(Long userId, NotificationEventResponse event) {
        SseEmitter emitter = emitters.get(userId);
        
        if (emitter == null || Boolean.TRUE.equals(completedEmitters.get(userId))) {
            log.debug("No active SSE connection for user: {}. Notification not sent.", userId);
            return;
        }

        try {
            SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
                    .name("notification")
                    .data(event);
            
            emitter.send(eventBuilder);
            log.info("Sent notification to user {}: type={}", userId, event.getType());
            
        } catch (IOException e) {
            log.error("Failed to send notification to user: {}: {}", userId, e.getMessage());
            removeEmitter(userId);
        } catch (IllegalStateException e) {
            log.warn("Emitter already completed for user: {}: {}", userId, e.getMessage());
            removeEmitter(userId);
        }
    }

    @Override
    public void sendVtuberApplicationNotification(Long userId, String status, String reason) {
        log.info("Sending VTuber application notification to user {}: status={}", userId, status);

        boolean isAccepted = "ACCEPTED".equalsIgnoreCase(status);
        
        NotificationEventResponse notification = NotificationEventResponse.builder()
                .id(System.currentTimeMillis()) // Temporary ID using timestamp
                .type(isAccepted ? "VTUBER_APPLICATION_APPROVED" : "VTUBER_APPLICATION_REJECTED")
                .title(isAccepted ? "Congratulations! 🎉" : "VTuber Application Update")
                .message(isAccepted 
                        ? "Your VTuber application has been approved! You can now create your FanHub."
                        : "Your VTuber application has been reviewed. Reason: " + (reason != null ? reason : "No reason provided"))
                .createdAt(Instant.now())
                .build();

        sendNotification(userId, notification);
    }

    @Override
    public void sendPostLikeNotification(Long postAuthorId, Long likedByUserId, String likedByUsername,
                                          String likedByAvatarUrl, Long postId, String postTitle,
                                          Long fanHubId, String fanHubName) {
        log.info("Sending post like notification: author={}, likedBy={}, postId={}", 
                postAuthorId, likedByUsername, postId);

        NotificationEventResponse notification = NotificationEventResponse.builder()
                .id(System.currentTimeMillis())
                .type("POST_LIKE")
                .title("New Like on Your Post")
                .message(String.format("%s liked your post \"%s\" in %s", 
                        likedByUsername, 
                        postTitle != null ? postTitle : "Untitled Post",
                        fanHubName != null ? fanHubName : "FanHub"))
                .relatedHubId(fanHubId)
                .relatedHubName(fanHubName)
                .relatedPostId(postId)
                .relatedPostTitle(postTitle)
                .triggeredByUserId(likedByUserId)
                .triggeredByUsername(likedByUsername)
                .triggeredByAvatarUrl(likedByAvatarUrl)
                .createdAt(Instant.now())
                .build();

        sendNotification(postAuthorId, notification);
    }

    @Override
    public void sendPostCommentNotification(Long postAuthorId, Long commentedByUserId, String commentedByUsername,
                                             String commentedByAvatarUrl, Long postId, String postTitle,
                                             Long fanHubId, String fanHubName) {
        log.info("Sending post comment notification: author={}, commentedBy={}, postId={}", 
                postAuthorId, commentedByUsername, postId);

        NotificationEventResponse notification = NotificationEventResponse.builder()
                .id(System.currentTimeMillis())
                .type("POST_COMMENT")
                .title("New Comment on Your Post")
                .message(String.format("%s commented on your post \"%s\" in %s", 
                        commentedByUsername,
                        postTitle != null ? postTitle : "Untitled Post",
                        fanHubName != null ? fanHubName : "FanHub"))
                .relatedHubId(fanHubId)
                .relatedHubName(fanHubName)
                .relatedPostId(postId)
                .relatedPostTitle(postTitle)
                .triggeredByUserId(commentedByUserId)
                .triggeredByUsername(commentedByUsername)
                .triggeredByAvatarUrl(commentedByAvatarUrl)
                .createdAt(Instant.now())
                .build();

        sendNotification(postAuthorId, notification);
    }

    @Override
    public void sendFanHubStrikeNotification(Long ownerUserId, Long hubId, String hubName, int strikeCount, String reason) {
        log.info("Sending FanHub strike notification to owner {}: hubId={}, strikeCount={}", 
                ownerUserId, hubId, strikeCount);

        NotificationEventResponse notification = NotificationEventResponse.builder()
                .id(System.currentTimeMillis())
                .type("FAN_HUB_STRIKE")
                .title("FanHub Strike Alert! ⚠️")
                .message(String.format("Your FanHub \"%s\" has received a strike. Total strikes: %d. Reason: %s",
                        hubName, strikeCount, reason != null ? reason : "Violation of community guidelines"))
                .relatedHubId(hubId)
                .relatedHubName(hubName)
                .createdAt(Instant.now())
                .build();

        sendNotification(ownerUserId, notification);
    }

    @Override
    public int getActiveEmitterCount() {
        return emitters.size();
    }

    public boolean hasActiveConnection(Long userId) {
        SseEmitter emitter = emitters.get(userId);
        return emitter != null && !Boolean.TRUE.equals(completedEmitters.get(userId));
    }
}
