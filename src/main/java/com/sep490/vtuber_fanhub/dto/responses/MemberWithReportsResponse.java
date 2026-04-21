package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class MemberWithReportsResponse {

    // Member information
    private Long memberId;
    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String roleInHub;
    private String memberStatus;

    // FanHub information
    private Long fanHubId;
    private String fanHubName;
    private String fanHubSubdomain;

    private Instant joinedAt;

    // Reports associated with this member
    private List<SimpleMemberReportResponse> reports;

    @Data
    public static class SimpleMemberReportResponse {
        private Long reportId;
        private Long reportedByUserId;
        private String reportedByUsername;
        private String reportedByDisplayName;
        private String reason;
        private String reportStatus;
        private String resolveMessage;
        private Instant reportCreatedAt;
        private Long resolvedByUserId;
        private String resolvedByUsername;
        private String resolvedByDisplayName;
        private RelatedCommentInfo relatedComment;

        @Data
        public static class RelatedCommentInfo {
            private Long commentId;
            private Long postId;
            private String content;
            private Instant createdAt;
        }
    }
}
