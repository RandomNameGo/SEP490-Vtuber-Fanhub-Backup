package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.NotificationEventResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface SseNotificationService {


    SseEmitter createEmitter(Long userId);


    void removeEmitter(Long userId);


    void sendNotification(Long userId, NotificationEventResponse event);


    void sendVtuberApplicationNotification(Long userId, String status, String reason);


    void sendPostLikeNotification(Long postAuthorId, Long likedByUserId, String likedByUsername,
                                   String likedByAvatarUrl, Long postId, String postTitle,
                                   Long fanHubId, String fanHubName);


    void sendPostCommentNotification(Long postAuthorId, Long commentedByUserId, String commentedByUsername,
                                      String commentedByAvatarUrl, Long postId, String postTitle,
                                      Long fanHubId, String fanHubName);

    void sendFanHubStrikeNotification(Long ownerUserId, Long hubId, String hubName, int strikeCount, String reason);

    int getActiveEmitterCount();
}
