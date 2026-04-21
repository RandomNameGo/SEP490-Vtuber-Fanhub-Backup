package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportPostRequest;
import com.sep490.vtuber_fanhub.dto.responses.ReportPostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostWithReportsResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostHashtag;
import com.sep490.vtuber_fanhub.models.PostMedia;
import com.sep490.vtuber_fanhub.models.ReportPost;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.PostHashtagRepository;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.ReportPostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportPostServiceImpl implements ReportPostService {

    private final ReportPostRepository reportPostRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    private final PostRepository postRepository;

    private final FanHubRepository fanHubRepository;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final PostMediaRepository postMediaRepository;

    private final PostHashtagRepository postHashtagRepository;

    private final NotificationService notificationService;

    @Override
    public String createReportPost(CreateReportPostRequest createReportPostRequest) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(createReportPostRequest.getPostId());
        if (post.isEmpty()) {
            throw new NotFoundException("Post not found");
        }

        ReportPost reportPost = new ReportPost();
        reportPost.setPost(post.get());
        reportPost.setReportedBy(currentUser);
        reportPost.setReason(createReportPostRequest.getReason());
        reportPost.setStatus("PENDING");
        reportPost.setCreatedAt(Instant.now());
        reportPostRepository.save(reportPost);

        return "Report post sent successfully";
    }

    @Override
    public List<ReportPostResponse> getReportPostsByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ReportPost> reportPostPage = reportPostRepository.findByFanHubId(fanHubId, pageRequest);

        return reportPostPage.getContent().stream()
                .map(this::mapToReportPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String resolveReportPost(Long reportId, String resolveMessage) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<ReportPost> reportPostOpt = reportPostRepository.findById(reportId);
        if (reportPostOpt.isEmpty()) {
            throw new NotFoundException("Report not found");
        }

        ReportPost reportPost = reportPostOpt.get();
        FanHub fanHub = reportPost.getPost().getHub();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHub.getId(), currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        // If reported user is the current user, they cannot resolve their own report
        if (reportPost.getPost().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot resolve your own report");
        }

        reportPost.setStatus("RESOLVED");
        reportPost.setResolveBy(currentUser);
        reportPost.setResolveMessage(resolveMessage);
        reportPostRepository.save(reportPost);

        // Send notification to the reporter
        notificationService.sendReportPostResolvedNotification(
                reportPost.getReportedBy().getId(),
                reportPost.getPost().getId(),
                reportPost.getPost().getTitle(),
                resolveMessage,
                currentUser.getId()
        );

        return "Report resolved successfully";
    }

    @Override
    public List<ReportPostResponse> getReportPostsByCurrentUser(int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ReportPost> reportPostPage = reportPostRepository.findByReportedById(currentUser.getId(), pageRequest);

        return reportPostPage.getContent().stream()
                .map(this::mapToReportPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportPostResponse> getPendingReportPostsByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ReportPost> reportPostPage = reportPostRepository.findByFanHubIdAndStatus(fanHubId, "PENDING", pageRequest);

        return reportPostPage.getContent().stream()
                .map(this::mapToReportPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String bulkResolveReportPosts(List<Long> reportIds, String resolveMessage) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        if (reportIds == null || reportIds.isEmpty()) {
            throw new IllegalArgumentException("Report IDs cannot be empty");
        }

        int resolvedCount = 0;
        for (Long reportId : reportIds) {
            Optional<ReportPost> reportPostOpt = reportPostRepository.findById(reportId);
            if (reportPostOpt.isEmpty()) {
                continue;
            }

            ReportPost reportPost = reportPostOpt.get();
            FanHub fanHub = reportPost.getPost().getHub();

            // Check if user is VTUBER and owns this FanHub
            boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                    fanHub.getOwnerUser().getId().equals(currentUser.getId());

            // Check if user is a member with MODERATOR role
            boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHub.getId(), currentUser.getId())
                    .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                    .orElse(false);

            if (!isOwner && !isModerator) {
                throw new AccessDeniedException("Access denied for report ID: " + reportId);
            }

            // If reported user is the current user, they cannot resolve their own report
            if (reportPost.getPost().getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Cannot resolve your own report for report ID: " + reportId);
            }

            reportPost.setStatus("RESOLVED");
            reportPost.setResolveBy(currentUser);
            reportPost.setResolveMessage(resolveMessage);
            reportPostRepository.save(reportPost);

            // Send notification to the reporter
            notificationService.sendReportPostResolvedNotification(
                    reportPost.getReportedBy().getId(),
                    reportPost.getPost().getId(),
                    reportPost.getPost().getTitle(),
                    resolveMessage,
                    currentUser.getId()
            );

            resolvedCount++;
        }

        return "Successfully resolved " + resolvedCount + " report(s)";
    }

    private ReportPostResponse mapToReportPostResponse(ReportPost reportPost) {
        ReportPostResponse response = new ReportPostResponse();
        
        // Report information
        response.setReportId(reportPost.getId());
        response.setReason(reportPost.getReason());
        response.setReportStatus(reportPost.getStatus());
        response.setReportCreatedAt(reportPost.getCreatedAt());
        
        // Post information
        Post post = reportPost.getPost();
        response.setPostId(post.getId());
        response.setPostType(post.getPostType());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setStatus(post.getStatus());
        response.setIsPinned(post.getIsPinned());
        response.setPostCreatedAt(post.getCreatedAt());
        response.setPostUpdatedAt(post.getUpdatedAt());
        
        // FanHub information
        FanHub fanHub = post.getHub();
        response.setFanHubId(fanHub.getId());
        response.setFanHubName(fanHub.getHubName());
        response.setFanHubSubdomain(fanHub.getSubdomain());
        
        // Author information
        User author = post.getUser();
        response.setAuthorId(author.getId());
        response.setAuthorUsername(author.getUsername());
        response.setAuthorDisplayName(author.getDisplayName());
        response.setAuthorAvatarUrl(author.getAvatarUrl());
        
        // Media count
        List<PostMedia> mediaList = postMediaRepository.findByPostId(post.getId());
        response.setMediaCount(mediaList.size());

        // Media URLs
        List<String> mediaUrls = mediaList.stream()
                .map(PostMedia::getMediaUrl)
                .collect(Collectors.toList());
        response.setMediaUrls(mediaUrls);

        // Hashtags
        List<String> hashtags = postHashtagRepository.findByPostId(post.getId())
                .stream()
                .map(PostHashtag::getHashtag)
                .collect(Collectors.toList());
        response.setHashtags(hashtags);
        
        // Reporter information
        response.setReportedByUserId(reportPost.getReportedBy().getId());
        response.setReportedByUsername(reportPost.getReportedBy().getUsername());
        response.setReportedByDisplayName(reportPost.getReportedBy().getDisplayName());
        
        // Resolver information (if resolved)
        if (reportPost.getResolveBy() != null) {
            response.setResolvedByUserId(reportPost.getResolveBy().getId());
            response.setResolvedByUsername(reportPost.getResolveBy().getUsername());
            response.setResolvedByDisplayName(reportPost.getResolveBy().getDisplayName());
        }
        response.setResolveMessage(reportPost.getResolveMessage());

        return response;
    }

    @Override
    public List<PostWithReportsResponse> getAllPostsWithReports(Long fanHubId, int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHub> fanHub = fanHubRepository.findById(fanHubId);
        if (fanHub.isEmpty()) {
            throw new NotFoundException("FanHub not found");
        }

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                fanHub.get().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        // Get all report posts for this fan hub with PENDING status
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ReportPost> reportPostPage = reportPostRepository.findByFanHubIdAndStatus(fanHubId, "PENDING", pageRequest);

        if (reportPostPage.isEmpty()) {
            return List.of();
        }

        // Group reports by post
        Map<Post, List<ReportPost>> postToReportsMap = reportPostPage.getContent().stream()
                .collect(Collectors.groupingBy(ReportPost::getPost));

        // Convert to response
        return postToReportsMap.entrySet().stream()
                .map(entry -> mapToPostWithReportsResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private PostWithReportsResponse mapToPostWithReportsResponse(Post post, List<ReportPost> reports) {
        PostWithReportsResponse response = new PostWithReportsResponse();

        // Post information
        response.setPostId(post.getId());
        response.setPostType(post.getPostType());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setStatus(post.getStatus());
        response.setIsPinned(post.getIsPinned());
        response.setIsAnnouncement(post.getIsAnnouncement());
        response.setIsSchedule(post.getIsSchedule());
        response.setPostCreatedAt(post.getCreatedAt());
        response.setPostUpdatedAt(post.getUpdatedAt());

        // FanHub information
        FanHub fanHub = post.getHub();
        response.setFanHubId(fanHub.getId());
        response.setFanHubName(fanHub.getHubName());
        response.setFanHubSubdomain(fanHub.getSubdomain());

        // Author information
        User author = post.getUser();
        response.setAuthorId(author.getId());
        response.setAuthorUsername(author.getUsername());
        response.setAuthorDisplayName(author.getDisplayName());
        response.setAuthorAvatarUrl(author.getAvatarUrl());

        // Media count
        List<PostMedia> mediaList = postMediaRepository.findByPostId(post.getId());
        response.setMediaCount(mediaList.size());

        // Media URLs
        List<String> mediaUrls = mediaList.stream()
                .map(PostMedia::getMediaUrl)
                .collect(Collectors.toList());
        response.setMediaUrls(mediaUrls);

        // Hashtags
        List<String> hashtags = postHashtagRepository.findByPostId(post.getId())
                .stream()
                .map(PostHashtag::getHashtag)
                .collect(Collectors.toList());
        response.setHashtags(hashtags);

        // Convert all reports to SimpleReportResponse
        List<PostWithReportsResponse.SimpleReportResponse> reportResponses = reports.stream()
                .map(this::mapToSimpleReportResponse)
                .collect(Collectors.toList());
        response.setReports(reportResponses);

        return response;
    }

    private PostWithReportsResponse.SimpleReportResponse mapToSimpleReportResponse(ReportPost reportPost) {
        PostWithReportsResponse.SimpleReportResponse response = new PostWithReportsResponse.SimpleReportResponse();

        // Report information
        response.setReportId(reportPost.getId());
        response.setReason(reportPost.getReason());
        response.setReportStatus(reportPost.getStatus());
        response.setReportCreatedAt(reportPost.getCreatedAt());
        response.setResolveMessage(reportPost.getResolveMessage());

        // Reporter information
        response.setReportedByUserId(reportPost.getReportedBy().getId());
        response.setReportedByUsername(reportPost.getReportedBy().getUsername());
        response.setReportedByDisplayName(reportPost.getReportedBy().getDisplayName());

        // Resolver information (if resolved)
        if (reportPost.getResolveBy() != null) {
            response.setResolvedByUserId(reportPost.getResolveBy().getId());
            response.setResolvedByUsername(reportPost.getResolveBy().getUsername());
            response.setResolvedByDisplayName(reportPost.getResolveBy().getDisplayName());
        }

        return response;
    }
}
