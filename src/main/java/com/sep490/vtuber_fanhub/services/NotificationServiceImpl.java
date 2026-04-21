package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.NotificationEventResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.Notification;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.NotificationRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.time.Instant;

/**
 * Service implementation for Notification management
 * Handles database persistence and coordinates with SSE service for real-time delivery
 * 
 * Features:
 * - Persist notifications to database for history
 * - Send real-time notifications via SSE
 * - Mark notifications as read/unread
 * - Pagination support for notification lists
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    private final SseNotificationService sseNotificationService;

    private final PostRepository postRepository;

    private final FanHubRepository fanHubRepository;

    private final AuthService authService;


    private User getCurrentUser(HttpServletRequest request) {
        return authService.getUserFromToken(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationEventResponse> getUserNotifications(HttpServletRequest request, int pageNo, int pageSize, String sortBy) {
        User user = getCurrentUser(request);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Notification> notificationPage = notificationRepository.findByUser(user, paging);

        if (notificationPage.hasContent()) {
            return notificationPage.getContent().stream()
                    .map(this::convertToEventResponse)
                    .toList();
        }
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationEventResponse> getUnreadNotifications(HttpServletRequest request, int pageNo, int pageSize, String sortBy) {
        User user = getCurrentUser(request);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Notification> notificationPage = notificationRepository.findByUserAndIsReadFalse(user, paging);

        if (notificationPage.hasContent()) {
            return notificationPage.getContent().stream()
                    .map(this::convertToEventResponse)
                    .toList();
        }
        return List.of();
    }

    private NotificationEventResponse convertToEventResponse(Notification notification) {
        return NotificationEventResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedHubId(notification.getRelatedHub() != null ? notification.getRelatedHub().getId() : null)
                .relatedHubName(notification.getRelatedHub() != null ? notification.getRelatedHub().getHubName() : null)
                .relatedPostId(notification.getRelatedPost() != null ? notification.getRelatedPost().getId() : null)
                .relatedPostTitle(notification.getRelatedPost() != null ? notification.getRelatedPost().getTitle() : null)
                .triggeredByUserId(notification.getTriggeredBy() != null ? notification.getTriggeredBy().getId() : null)
                .triggeredByUsername(notification.getTriggeredBy() != null ? notification.getTriggeredBy().getUsername() : null)
                .triggeredByAvatarUrl(notification.getTriggeredBy() != null ? notification.getTriggeredBy().getAvatarUrl() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadNotificationCount(HttpServletRequest request) {
        User user = getCurrentUser(request);

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public String markAsRead(Long notificationId, HttpServletRequest request) {
        User user = getCurrentUser(request);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        // Verify ownership
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to mark this notification as read");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        log.info("Marked notification {} as read for user {}", notificationId, user.getId());
        return "Notification marked as read";
    }

    @Override
    @Transactional
    public int markAllAsRead(HttpServletRequest request) {
        User user = getCurrentUser(request);
        int count = notificationRepository.markAllAsRead(user.getId());
        log.info("Marked {} notifications as read for user {}", count, user.getId());
        return count;
    }

    @Override
    @Transactional
    public String deleteNotification(Long notificationId, HttpServletRequest request) {
        User user = getCurrentUser(request);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        // Verify ownership
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this notification");
        }

        notificationRepository.delete(notification);
        log.info("Deleted notification {} for user {}", notificationId, user.getId());
        return "Notification deleted";
    }

    @Override
    @Transactional
    public String deleteAllNotifications(HttpServletRequest request) {
        User user = getCurrentUser(request);

        notificationRepository.deleteByUser(user);
        log.info("Deleted all notifications for user {}", user.getId());
        return "All notifications deleted";
    }

    @Override
    @Transactional
    public Notification createNotification(User user, String type, String title, String message,
                                            FanHub relatedHub, Post relatedPost, User triggeredBy) {
        // Generate unique ID for notification
        Long maxId = notificationRepository.findMaxId();
        Long newId = (maxId == null) ? 1L : maxId + 1;
        
        Notification notification = new Notification();
        notification.setId(newId);
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedHub(relatedHub);
        notification.setRelatedPost(relatedPost);
        notification.setTriggeredBy(triggeredBy);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        
        notification = notificationRepository.save(notification);
        log.debug("Created notification {} for user {}: type={}", newId, user.getId(), type);
        
        // Create SSE event DTO and send via SSE
        NotificationEventResponse eventDto = NotificationEventResponse.builder()
                .id(newId)
                .type(type)
                .title(title)
                .message(message)
                .relatedHubId(relatedHub != null ? relatedHub.getId() : null)
                .relatedHubName(relatedHub != null ? relatedHub.getHubName() : null)
                .relatedPostId(relatedPost != null ? relatedPost.getId() : null)
                .relatedPostTitle(relatedPost != null ? relatedPost.getTitle() : null)
                .triggeredByUserId(triggeredBy != null ? triggeredBy.getId() : null)
                .triggeredByUsername(triggeredBy != null ? triggeredBy.getUsername() : null)
                .triggeredByAvatarUrl(triggeredBy != null ? triggeredBy.getAvatarUrl() : null)
                .isRead(false)
                .createdAt(Instant.now())
                .build();
        
        // Send via SSE if user has active connection
        sseNotificationService.sendNotification(user.getId(), eventDto);
        
        return notification;
    }

    @Override
    @Transactional
    public void sendVtuberApplicationNotification(Long userId, String status, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        boolean isAccepted = "ACCEPTED".equalsIgnoreCase(status);
        String type = isAccepted ? "VTUBER_APPLICATION_APPROVED" : "VTUBER_APPLICATION_REJECTED";
        String title = isAccepted ? "Congratulations! 🎉" : "VTuber Application Update";
        String message = isAccepted 
                ? "Your VTuber application has been approved! You can now create your FanHub."
                : "Your VTuber application has been rejected. Reason: " + (reason != null ? reason : "No reason provided");
        
        // Create and persist notification, also sends via SSE
        createNotification(user, type, title, message, null, null, null);
        
        log.info("Sent VTuber application notification to user {}: {}", userId, status);
    }

    @Override
    @Transactional
    public void sendPostLikeNotification(Long postAuthorId, Long likedByUserId, String likedByUsername,
                                          String likedByAvatarUrl, Long postId, String postTitle,
                                          Long fanHubId, String fanHubName) {
        User postAuthor = userRepository.findById(postAuthorId)
                .orElseThrow(() -> new NotFoundException("Post author not found"));

        User likedBy = userRepository.findById(likedByUserId)
                .orElseThrow(() -> new NotFoundException("User who liked not found"));

        // Fetch the related Post entity to properly associate with notification
        Post relatedPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        // Fetch the related FanHub entity to properly associate with notification
        FanHub relatedHub = fanHubRepository.findById(fanHubId)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        String type = "POST_LIKE";
        String title = "New Like on Your Post";
        String message = String.format("%s liked your post",
                likedByUsername);

        // Create notification with related Post and FanHub entities
        // This ensures the notification properly references the post and fanhub in database
        createNotification(postAuthor, type, title, message, relatedHub, relatedPost, likedBy);

        log.info("Sent post like notification to user {} from user {} for post {} in hub {}", 
                postAuthorId, likedByUserId, postId, fanHubId);
    }

    @Override
    @Transactional
    public void sendPostCommentNotification(Long postAuthorId, Long commentedByUserId, String commentedByUsername,
                                             String commentedByAvatarUrl, Long postId, String postTitle,
                                             Long fanHubId, String fanHubName) {
        User postAuthor = userRepository.findById(postAuthorId)
                .orElseThrow(() -> new NotFoundException("Post author not found"));

        User commentedBy = userRepository.findById(commentedByUserId)
                .orElseThrow(() -> new NotFoundException("User who commented not found"));

        // Fetch the related Post entity to properly associate with notification
        Post relatedPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        // Fetch the related FanHub entity to properly associate with notification
        FanHub relatedHub = fanHubRepository.findById(fanHubId)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        String type = "POST_COMMENT";
        String title = "New Comment on Your Post";
        String message = String.format("%s commented on your post ",
                commentedByUsername);

        // Create notification with related Post and FanHub entities
        // This ensures the notification properly references the post and fanhub in database
        createNotification(postAuthor, type, title, message, relatedHub, relatedPost, commentedBy);

        log.info("Sent post comment notification to user {} from user {} for post {} in hub {}", 
                postAuthorId, commentedByUserId, postId, fanHubId);
    }

    @Override
    @Transactional
    public void sendFanHubStrikeNotification(Long ownerUserId, Long hubId, String hubName, int strikeCount, String reason) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new NotFoundException("FanHub owner not found"));

        FanHub hub = fanHubRepository.findById(hubId)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        String type = "FAN_HUB_STRIKE";
        String title = "FanHub Strike Alert! ⚠️";
        String message = String.format("Your FanHub \"%s\" has received a strike. Total strikes: %d. Reason: %s",
                hubName, strikeCount, reason != null ? reason : "Violation of community guidelines");

        // Create and persist notification
        createNotification(owner, type, title, message, hub, null, null);

        log.info("Sent FanHub strike notification to owner {} for hub {}. Total strikes: {}", 
                ownerUserId, hubId, strikeCount);
    }

    @Override
    @Transactional
    public void sendMemberBannedNotification(Long userId, Long fanHubId, String fanHubName, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        FanHub hub = fanHubRepository.findById(fanHubId)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        String type = "MEMBER_BANNED";
        String title = "Banned from FanHub";
        String message = String.format("You have been banned from FanHub \"%s\". Reason: %s",
                fanHubName, reason != null ? reason : "Violation of community guidelines");

        createNotification(user, type, title, message, hub, null, null);

        log.info("Sent member banned notification to user {} for hub {}", userId, fanHubId);
    }

    @Override
    @Transactional
    public void sendMemberAcceptedNotification(Long userId, Long fanHubId, String fanHubName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        FanHub hub = fanHubRepository.findById(fanHubId)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        String type = "MEMBER_ACCEPTED";
        String title = "Welcome to the FanHub! 🎉";
        String message = String.format("Your request to join FanHub \"%s\" has been accepted. Welcome aboard!",
                fanHubName);

        createNotification(user, type, title, message, hub, null, null);

        log.info("Sent member accepted notification to user {} for hub {}", userId, fanHubId);
    }

    @Override
    @Transactional
    public void sendReportPostResolvedNotification(Long reporterId, Long postId, String postTitle, String resolution, Long resolvedByUserId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("Reporter not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        User resolvedBy = userRepository.findById(resolvedByUserId)
                .orElseThrow(() -> new NotFoundException("Moderator not found"));

        String type = "REPORT_POST_RESOLVED";
        String title = "Report Update";
        String message = String.format("Your report on post \"%s\" has been resolved. Resolution: %s",
                postTitle, resolution != null ? resolution : "The issue has been addressed by a moderator.");

        createNotification(reporter, type, title, message, post.getHub(), post, resolvedBy);

        log.info("Sent report post resolved notification to user {} for post {} resolved by {}", reporterId, postId, resolvedByUserId);
    }

    @Override
    @Transactional
    public void sendReportMemberResolvedNotification(Long reporterId, Long reportedMemberId, Long fanHubId, String resolution, Long resolvedByUserId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("Reporter not found"));

        User reportedMember = userRepository.findById(reportedMemberId)
                .orElseThrow(() -> new NotFoundException("Reported member not found"));

        User resolvedBy = userRepository.findById(resolvedByUserId)
                .orElseThrow(() -> new NotFoundException("Moderator not found"));

        FanHub relatedHub = fanHubRepository.findById(fanHubId)
                .orElseThrow(() -> new NotFoundException("FanHub not found"));

        String type = "REPORT_MEMBER_RESOLVED";
        String title = "Report Update";
        String message = String.format("Your report on user %s in \"%s\" has been resolved. Resolution: %s",
                reportedMember.getDisplayName(), relatedHub.getHubName(), resolution != null ? resolution : "The issue has been addressed by a moderator.");

        createNotification(reporter, type, title, message, relatedHub, null, resolvedBy);

        log.info("Sent report member resolved notification to user {} for reported user {} in hub {} resolved by {}", reporterId, reportedMemberId, fanHubId, resolvedByUserId);
    }
}
