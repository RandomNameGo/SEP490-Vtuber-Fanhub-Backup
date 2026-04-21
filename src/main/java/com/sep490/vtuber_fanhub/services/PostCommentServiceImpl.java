package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostCommentRequest;
import com.sep490.vtuber_fanhub.dto.requests.EditPostCommentRequest;
import com.sep490.vtuber_fanhub.dto.responses.PostCommentResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostComment;
import com.sep490.vtuber_fanhub.models.PostCommentGift;
import com.sep490.vtuber_fanhub.models.PostCommentLike;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserDailyMission;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.PostCommentGiftRepository;
import com.sep490.vtuber_fanhub.repositories.PostCommentLikeRepository;
import com.sep490.vtuber_fanhub.repositories.PostCommentRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.UserDailyMissionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Post Comment management
 * Handles comment creation, likes, and gifts
 * Sends SSE notifications for new comments on posts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;

    private final PostRepository postRepository;

    private final HttpServletRequest httpServletRequest;

    private final AuthService authService;

    private final PostCommentLikeRepository postCommentLikeRepository;

    private final PostCommentGiftRepository postCommentGiftRepository;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final FanHubRepository fanHubRepository;

    private final UserDailyMissionRepository userDailyMissionRepository;

    private final UserRepository userRepository;

    private final UserTrackService userTrackService;

    private final UserDailyMissionService userDailyMissionService;

    //SSE
    private final NotificationService notificationService;

    private final BanMemberService banMemberService;

    @Override
    @Transactional
    public boolean createPostComment(CreatePostCommentRequest createPostCommentRequest) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(createPostCommentRequest.getPostId());
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        // Check if user is banned from commenting in this hub
        banMemberService.checkBanStatus(
                post.get().getHub().getId(),
                currentUser.getId(),
                List.of("COMMENT"));

        // Validate membership: user must be the hub owner or a member of the hub
        FanHub hub = post.get().getHub();
        boolean isHubOwner = hub.getOwnerUser().getId().equals(currentUser.getId());
        Optional<FanHubMember> member = fanHubMemberRepository.findByHubIdAndUserId(
                hub.getId(), currentUser.getId());

        if (!isHubOwner && member.isEmpty()) {
            throw new AccessDeniedException("You must be the owner (VTuber) or a member of this FanHub to comment");
        }

        PostComment postComment = new PostComment();
        postComment.setPost(post.get());
        postComment.setUser(currentUser);
        postComment.setContent(createPostCommentRequest.getContent());
        postComment.setStatus("VISIBLE");
        postComment.setCreatedAt(Instant.now());

        // Set memberId if user is a member, otherwise use null for hub owner
        member.ifPresent(fanHubMember -> postComment.setMemberId(fanHubMember.getId()));

        if (createPostCommentRequest.getParentCommentId() != null) {
            Optional<PostComment> parentComment = postCommentRepository.findById(createPostCommentRequest.getParentCommentId());
            parentComment.ifPresent(postComment::setParentComment);
        }

        postCommentRepository.save(postComment);

        // Update user track
        userTrackService.updateOnComment(currentUser);

        // Send SSE notification to post author about the new comment
        // Only send if the commenter is not the post author themselves
        // Also persists notification to database
        User postAuthor = post.get().getUser();
        if (!postAuthor.getId().equals(currentUser.getId())) {
            notificationService.sendPostCommentNotification(
                    postAuthor.getId(),
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getAvatarUrl(),
                    post.get().getId(),
                    post.get().getTitle(),
                    post.get().getHub().getId(),
                    post.get().getHub().getHubName()
            );
            log.info("Sent SSE notification to post author {} for comment from user {}", 
                    postAuthor.getId(), currentUser.getId());
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostCommentResponse> getPostCommentsByPostId(Long postId) {
        User currentUser = null;
        try {
            currentUser = authService.getUserFromToken(httpServletRequest);
        } catch (Exception e) {
            // User not logged in or invalid token, treat as guest
            currentUser = null;
        }

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        List<PostComment> comments = postCommentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);

        if (currentUser == null) {
            return comments.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        // Prioritize current user's comments to the top
        final Long currentUserId = currentUser.getId();
        
        List<PostComment> prioritizedComments = comments.stream()
                .filter(c -> c.getUser().getId().equals(currentUserId))
                .collect(Collectors.toList());
        
        List<PostComment> otherComments = comments.stream()
                .filter(c -> !c.getUser().getId().equals(currentUserId))
                .collect(Collectors.toList());

        prioritizedComments.addAll(otherComments);

        return prioritizedComments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostCommentResponse> getCommentsByParentId(Long parentCommentId) {
        Optional<PostComment> parentComment = postCommentRepository.findById(parentCommentId);
        if (parentComment.isEmpty()) {
            throw new NotFoundException("Parent comment not found");
        }

        List<PostComment> comments = postCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentCommentId);

        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String editComment(Long commentId, EditPostCommentRequest request) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<PostComment> comment = postCommentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new NotFoundException("Comment not found");
        }

        if (!comment.get().getUser().getId().equals(currentUser.getId())) {
            throw new CustomAuthenticationException("Access denied. Only the author can edit this comment.");
        }

        comment.get().setContent(request.getContent());
        postCommentRepository.save(comment.get());

        return "Comment edited successfully";
    }

    @Override
    @Transactional
    public String deleteComment(Long commentId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<PostComment> comment = postCommentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new NotFoundException("Comment not found");
        }

        if (!comment.get().getUser().getId().equals(currentUser.getId())) {
            throw new CustomAuthenticationException("Access denied. Only the author can delete this comment.");
        }

        postCommentRepository.delete(comment.get());

        return "Comment deleted successfully";
    }

    private PostCommentResponse mapToResponse(PostComment comment) {
        PostCommentResponse response = new PostCommentResponse();
        response.setCommentId(comment.getId());
        response.setPostId(comment.getPost().getId());

        response.setUserId(comment.getUser().getId());
        response.setUsername(comment.getUser().getUsername());
        response.setDisplayName(comment.getUser().getDisplayName());
        response.setAvatarUrl(comment.getUser().getAvatarUrl());
        response.setMemberId(comment.getMemberId());

        response.setContent(comment.getContent());
        response.setStatus(comment.getStatus());
        response.setCreatedAt(comment.getCreatedAt());

        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }

        // Get like count
        Long likeCount = postCommentLikeRepository.countByComment(comment);
        response.setLikeCount(likeCount);

        // Check if current user liked this comment
        try {
            User currentUser = authService.getUserFromToken(httpServletRequest);
            Boolean isLiked = postCommentLikeRepository.findByUserIdAndComment(currentUser.getId(), comment).isPresent();
            response.setIsLikedByCurrentUser(isLiked);
        } catch (Exception e) {
            response.setIsLikedByCurrentUser(false);
        }

        // Get gift count for this comment
        List<PostCommentGift> gifts = postCommentGiftRepository.findByComment(comment);
        response.setGiftCount((long) gifts.size());

        // Check if comment has children (replies)
        boolean hasChildren = postCommentRepository.existsByParentCommentId(comment.getId());
        response.setHasChildren(hasChildren);

        return response;
    }

    @Override
    @Transactional
    public String likeComment(Long commentId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<PostComment> comment = postCommentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new NotFoundException("Comment not found");
        }

        Long userId = currentUser.getId();

        // Check if user already liked this comment
        Optional<PostCommentLike> existingLike = postCommentLikeRepository.findByUserIdAndComment(userId, comment.get());
        if (existingLike.isPresent()) {
            throw new IllegalArgumentException("You have already liked this comment");
        }

        // Create and save the like
        PostCommentLike postCommentLike = new PostCommentLike();
        postCommentLike.setUser(currentUser);
        postCommentLike.setComment(comment.get());
        postCommentLike.setCreatedAt(Instant.now());
        postCommentLikeRepository.save(postCommentLike);


        return "Comment liked successfully!";
    }

    @Override
    @Transactional
    public String unlikeComment(Long commentId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<PostComment> comment = postCommentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new NotFoundException("Comment not found");
        }

        Long userId = currentUser.getId();

        Optional<PostCommentLike> existingLike = postCommentLikeRepository.findByUserIdAndComment(userId, comment.get());
        if (existingLike.isEmpty()) {
            throw new IllegalArgumentException("You have not liked this comment");
        }

        postCommentLikeRepository.delete(existingLike.get());

        return "Comment unliked successfully.";
    }

    @Override
    @Transactional
    public String sendCommentGift(Long commentId) {
        User sender = authService.getUserFromToken(httpServletRequest);

        Optional<PostComment> comment = postCommentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new NotFoundException("Comment not found");
        }

        User receiver = comment.get().getUser();

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("You cannot send a gift to your own comment");
        }

        long senderPoints = sender.getPoints() != null ? sender.getPoints() : 0L;
        if (senderPoints < 2) {
            throw new IllegalArgumentException("Insufficient points to send a gift (requires 2 points)");
        }
        sender.setPoints(senderPoints - 2);
        userRepository.save(sender);

        Optional<PostCommentGift> existingGift = postCommentGiftRepository.findBySenderAndComment(sender, comment.get());

        if (existingGift.isPresent()) {
            PostCommentGift gift = existingGift.get();
            long currentAmount = gift.getAmount() != null ? gift.getAmount() : 0L;
            gift.setAmount(currentAmount + 2);
            postCommentGiftRepository.save(gift);
        } else {
            PostCommentGift gift = new PostCommentGift();
            gift.setSender(sender);
            gift.setComment(comment.get());
            gift.setReceiver(receiver);
            gift.setAmount(2L);
            gift.setReceiveAt(Instant.now());
            postCommentGiftRepository.save(gift);
        }

        long receiverPoints = receiver.getPoints() != null ? receiver.getPoints() : 0L;
        receiver.setPoints(receiverPoints + 2);
        userRepository.save(receiver);

        return "Gift sent successfully!";
    }

    @Override
    @Transactional
    public String hideComment(Long commentId) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        FanHub hub = comment.getPost().getHub();
        Long hubId = hub.getId();

        // Check if user is the VTuber owner of this FanHub
        boolean isOwner = hub.getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role in this hub
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(hubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Only the FanHub owner or a moderator can hide comments");
        }

        comment.setStatus("HIDDEN");
        postCommentRepository.save(comment);

        log.info("Comment {} in hub {} has been hidden by user {}", commentId, hubId, currentUser.getId());
        return "Comment hidden successfully";
    }
}
