package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.NotificationEventResponse;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.Notification;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;


public interface NotificationService {


    List<NotificationEventResponse> getUserNotifications(HttpServletRequest request, int pageNo, int pageSize, String sortBy);


    List<NotificationEventResponse> getUnreadNotifications(HttpServletRequest request, int pageNo, int pageSize, String sortBy);


    Long getUnreadNotificationCount(HttpServletRequest request);


    String markAsRead(Long notificationId, HttpServletRequest request);


    int markAllAsRead(HttpServletRequest request);


    String deleteNotification(Long notificationId, HttpServletRequest request);

    String deleteAllNotifications(HttpServletRequest request);


    Notification createNotification(User user, String type, String title, String message,
                                     FanHub relatedHub, Post relatedPost, User triggeredBy);


    void sendVtuberApplicationNotification(Long userId, String status, String reason);


    void sendPostLikeNotification(Long postAuthorId, Long likedByUserId, String likedByUsername,
                                   String likedByAvatarUrl, Long postId, String postTitle,
                                   Long fanHubId, String fanHubName);

    void sendPostCommentNotification(Long postAuthorId, Long commentedByUserId, String commentedByUsername,
                                      String commentedByAvatarUrl, Long postId, String postTitle,
                                      Long fanHubId, String fanHubName);

    void sendFanHubStrikeNotification(Long ownerUserId, Long hubId, String hubName, int strikeCount, String reason);

    void sendMemberBannedNotification(Long userId, Long fanHubId, String fanHubName, String reason);

    void sendMemberAcceptedNotification(Long userId, Long fanHubId, String fanHubName);

    void sendReportPostResolvedNotification(Long reporterId, Long postId, String postTitle, String resolution, Long resolvedByUserId);

    void sendReportMemberResolvedNotification(Long reporterId, Long reportedMemberId, Long fanHubId, String resolution, Long resolvedByUserId);
}
