package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportMemberRequest;
import com.sep490.vtuber_fanhub.dto.responses.ReportMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberWithReportsResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.PostComment;
import com.sep490.vtuber_fanhub.models.ReportMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.PostCommentRepository;
import com.sep490.vtuber_fanhub.repositories.ReportMemberRepository;
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
public class ReportMemberServiceImpl implements ReportMemberService {

    private final ReportMemberRepository reportMemberRepository;

    private final AuthService authService;

    private final HttpServletRequest httpServletRequest;

    private final FanHubMemberRepository fanHubMemberRepository;

    private final FanHubRepository fanHubRepository;

    private final PostCommentRepository postCommentRepository;

    private final NotificationService notificationService;

    @Override
    public String createReportMember(CreateReportMemberRequest createReportMemberRequest) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<FanHubMember> fanHubMember = fanHubMemberRepository.findById(createReportMemberRequest.getMemberId());
        if (fanHubMember.isEmpty()) {
            throw new NotFoundException("Member not found");
        }

        ReportMember reportMember = new ReportMember();
        reportMember.setUser(fanHubMember.get().getUser());
        reportMember.setReportedBy(currentUser);
        reportMember.setHub(fanHubMember.get().getHub());
        reportMember.setReason(createReportMemberRequest.getReason());
        reportMember.setStatus("PENDING");
        reportMember.setCreatedAt(Instant.now());

        if (createReportMemberRequest.getRelatedCommentId() != null) {
            Optional<PostComment> postComment = postCommentRepository.findById(createReportMemberRequest.getRelatedCommentId());
            if (postComment.isPresent()) {
                reportMember.setRelatedComment(postComment.get());
            }
        }

        reportMemberRepository.save(reportMember);

        return "Report member sent successfully";
    }

    @Override
    public List<ReportMemberResponse> getReportMembersByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy) {
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
        Page<ReportMember> reportMemberPage = reportMemberRepository.findByFanHubId(fanHubId, pageRequest);

        return reportMemberPage.getContent().stream()
                .map(this::mapToReportMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String resolveReportMember(Long reportId, String resolveMessage) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<ReportMember> reportMemberOpt = reportMemberRepository.findById(reportId);
        if (reportMemberOpt.isEmpty()) {
            throw new NotFoundException("Report not found");
        }

        ReportMember reportMember = reportMemberOpt.get();

        // Check if user is VTUBER and owns this FanHub
        boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                reportMember.getHub().getOwnerUser().getId().equals(currentUser.getId());

        // Check if user is a member with MODERATOR role
        boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(reportMember.getHub().getId(), currentUser.getId())
                .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                .orElse(false);

        if (!isOwner && !isModerator) {
            throw new AccessDeniedException("Access denied");
        }

        // If reported user is the current user, they cannot resolve their own report
        if (reportMember.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot resolve your own report");
        }

        reportMember.setStatus("RESOLVED");
        reportMember.setResolveBy(currentUser);
        reportMember.setResolveMessage(resolveMessage);
        reportMemberRepository.save(reportMember);

        // Send notification to the reporter
        notificationService.sendReportMemberResolvedNotification(
                reportMember.getReportedBy().getId(),
                reportMember.getUser().getId(),
                reportMember.getHub().getId(),
                resolveMessage,
                currentUser.getId()
        );

        return "Report resolved successfully";
    }

    @Override
    public List<ReportMemberResponse> getReportMembersByCurrentUser(int pageNo, int pageSize, String sortBy) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ReportMember> reportMemberPage = reportMemberRepository.findByReportedById(currentUser.getId(), pageRequest);

        return reportMemberPage.getContent().stream()
                .map(this::mapToReportMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportMemberResponse> getPendingReportMembersByFanHubId(Long fanHubId, int pageNo, int pageSize, String sortBy) {
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
        Page<ReportMember> reportMemberPage = reportMemberRepository.findByHubIdAndStatus(fanHubId, "PENDING", pageRequest);

        return reportMemberPage.getContent().stream()
                .map(this::mapToReportMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String bulkResolveReportMembers(List<Long> reportIds, String resolveMessage) {
        User currentUser = authService.getUserFromToken(httpServletRequest);

        if (reportIds == null || reportIds.isEmpty()) {
            throw new IllegalArgumentException("Report IDs cannot be empty");
        }

        int resolvedCount = 0;
        for (Long reportId : reportIds) {
            Optional<ReportMember> reportMemberOpt = reportMemberRepository.findById(reportId);
            if (reportMemberOpt.isEmpty()) {
                continue;
            }

            ReportMember reportMember = reportMemberOpt.get();

            // Check if user is VTUBER and owns this FanHub
            boolean isOwner = "VTUBER".equals(currentUser.getRole()) &&
                    reportMember.getHub().getOwnerUser().getId().equals(currentUser.getId());

            // Check if user is a member with MODERATOR role
            boolean isModerator = fanHubMemberRepository.findByHubIdAndUserId(reportMember.getHub().getId(), currentUser.getId())
                    .map(member -> "MODERATOR".equals(member.getRoleInHub()))
                    .orElse(false);

            if (!isOwner && !isModerator) {
                throw new AccessDeniedException("Access denied for report ID: " + reportId);
            }

            // If reported user is the current user, they cannot resolve their own report
            if (reportMember.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Cannot resolve your own report for report ID: " + reportId);
            }

            reportMember.setStatus("RESOLVED");
            reportMember.setResolveBy(currentUser);
            reportMember.setResolveMessage(resolveMessage);
            reportMemberRepository.save(reportMember);

            // Send notification to the reporter
            notificationService.sendReportMemberResolvedNotification(
                    reportMember.getReportedBy().getId(),
                    reportMember.getUser().getId(),
                    reportMember.getHub().getId(),
                    resolveMessage,
                    currentUser.getId()
            );

            resolvedCount++;
        }

        return "Successfully resolved " + resolvedCount + " report(s)";
    }

    private ReportMemberResponse mapToReportMemberResponse(ReportMember reportMember) {
        ReportMemberResponse response = new ReportMemberResponse();
        response.setReportId(reportMember.getId());
        response.setReportedUserId(reportMember.getUser().getId());
        response.setReportedUsername(reportMember.getUser().getUsername());
        response.setReportedDisplayName(reportMember.getUser().getDisplayName());
        response.setFanHubId(reportMember.getHub().getId());
        response.setFanHubName(reportMember.getHub().getHubName());
        response.setReportedByUserId(reportMember.getReportedBy().getId());
        response.setReportedByUsername(reportMember.getReportedBy().getUsername());
        response.setReportedByDisplayName(reportMember.getReportedBy().getDisplayName());
        response.setReason(reportMember.getReason());
        response.setStatus(reportMember.getStatus());
        response.setCreatedAt(reportMember.getCreatedAt());

        if (reportMember.getResolveBy() != null) {
            response.setResolvedByUserId(reportMember.getResolveBy().getId());
            response.setResolvedByUsername(reportMember.getResolveBy().getUsername());
            response.setResolvedByDisplayName(reportMember.getResolveBy().getDisplayName());
        }
        response.setResolveMessage(reportMember.getResolveMessage());

        if (reportMember.getRelatedComment() != null) {
            PostComment comment = reportMember.getRelatedComment();
            ReportMemberResponse.RelatedCommentInfo commentInfo = new ReportMemberResponse.RelatedCommentInfo();
            commentInfo.setCommentId(comment.getId());
            commentInfo.setPostId(comment.getPost().getId());
            commentInfo.setUserId(comment.getUser().getId());
            commentInfo.setUsername(comment.getUser().getUsername());
            commentInfo.setDisplayName(comment.getUser().getDisplayName());
            commentInfo.setContent(comment.getContent());
            commentInfo.setCreatedAt(comment.getCreatedAt());
            response.setRelatedComment(commentInfo);
        }

        return response;
    }

    @Override
    public List<MemberWithReportsResponse> getAllMembersWithReports(Long fanHubId, int pageNo, int pageSize, String sortBy) {
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

        // Get all report members for this fan hub with PENDING status
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<ReportMember> reportMemberPage = reportMemberRepository.findByHubIdAndStatus(fanHubId, "PENDING", pageRequest);

        if (reportMemberPage.isEmpty()) {
            return List.of();
        }

        // Group reports by member (user)
        Map<User, List<ReportMember>> userToReportsMap = reportMemberPage.getContent().stream()
                .collect(Collectors.groupingBy(ReportMember::getUser));

        // Convert to response
        return userToReportsMap.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<ReportMember> reports = entry.getValue();
                    // Get the FanHubMember for this user and hub
                    Optional<FanHubMember> fanHubMember = fanHubMemberRepository.findByHubIdAndUserId(fanHubId, user.getId());
                    return mapToMemberWithReportsResponse(user, fanHubMember, fanHub.get(), reports);
                })
                .collect(Collectors.toList());
    }

    private MemberWithReportsResponse mapToMemberWithReportsResponse(User user, Optional<FanHubMember> fanHubMemberOpt, FanHub fanHub, List<ReportMember> reports) {
        MemberWithReportsResponse response = new MemberWithReportsResponse();

        // User information
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setAvatarUrl(user.getAvatarUrl());

        // FanHubMember information (if exists)
        if (fanHubMemberOpt.isPresent()) {
            FanHubMember fanHubMember = fanHubMemberOpt.get();
            response.setMemberId(fanHubMember.getId());
            response.setRoleInHub(fanHubMember.getRoleInHub());
            response.setMemberStatus(fanHubMember.getStatus());
            response.setJoinedAt(fanHubMember.getJoinedAt());
        }

        // FanHub information
        response.setFanHubId(fanHub.getId());
        response.setFanHubName(fanHub.getHubName());
        response.setFanHubSubdomain(fanHub.getSubdomain());

        // Convert all reports to SimpleMemberReportResponse
        List<MemberWithReportsResponse.SimpleMemberReportResponse> reportResponses = reports.stream()
                .map(this::mapToSimpleMemberReportResponse)
                .collect(Collectors.toList());
        response.setReports(reportResponses);

        return response;
    }

    private MemberWithReportsResponse.SimpleMemberReportResponse mapToSimpleMemberReportResponse(ReportMember reportMember) {
        MemberWithReportsResponse.SimpleMemberReportResponse response = new MemberWithReportsResponse.SimpleMemberReportResponse();

        // Report information
        response.setReportId(reportMember.getId());
        response.setReason(reportMember.getReason());
        response.setReportStatus(reportMember.getStatus());
        response.setReportCreatedAt(reportMember.getCreatedAt());
        response.setResolveMessage(reportMember.getResolveMessage());

        // Reporter information
        response.setReportedByUserId(reportMember.getReportedBy().getId());
        response.setReportedByUsername(reportMember.getReportedBy().getUsername());
        response.setReportedByDisplayName(reportMember.getReportedBy().getDisplayName());

        // Resolver information (if resolved)
        if (reportMember.getResolveBy() != null) {
            response.setResolvedByUserId(reportMember.getResolveBy().getId());
            response.setResolvedByUsername(reportMember.getResolveBy().getUsername());
            response.setResolvedByDisplayName(reportMember.getResolveBy().getDisplayName());
        }

        // Related comment (if exists)
        if (reportMember.getRelatedComment() != null) {
            PostComment comment = reportMember.getRelatedComment();
            MemberWithReportsResponse.SimpleMemberReportResponse.RelatedCommentInfo commentInfo = 
                    new MemberWithReportsResponse.SimpleMemberReportResponse.RelatedCommentInfo();
            commentInfo.setCommentId(comment.getId());
            commentInfo.setPostId(comment.getPost().getId());
            commentInfo.setContent(comment.getContent());
            commentInfo.setCreatedAt(comment.getCreatedAt());
            response.setRelatedComment(commentInfo);
        }

        return response;
    }
}
