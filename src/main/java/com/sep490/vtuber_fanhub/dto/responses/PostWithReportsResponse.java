package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PostWithReportsResponse {

    // Post information
    private Long postId;
    private Long fanHubId;
    private String fanHubName;
    private String fanHubSubdomain;

    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;

    private String postType;
    private String title;
    private String content;
    private String status;
    private Boolean isPinned;
    private Boolean isAnnouncement;
    private Boolean isSchedule;

    private Integer mediaCount;
    private List<String> hashtags;
    private List<String> mediaUrls;

    private Instant postCreatedAt;
    private Instant postUpdatedAt;

    // Reports associated with this post
    private List<SimpleReportResponse> reports;

    @Data
    public static class SimpleReportResponse {
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
    }
}
