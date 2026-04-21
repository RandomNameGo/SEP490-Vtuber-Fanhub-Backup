package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class FanHubWithReportsResponse {

    // FanHub information
    private Long fanHubId;
    private String hubName;
    private String subdomain;
    private String description;
    private String bannerUrl;
    private String avatarUrl;
    private Boolean isActive;
    private Boolean isPrivate;
    private Integer strikeCount;
    private Instant createdAt;

    // Owner information
    private Long ownerUserId;
    private String ownerUsername;
    private String ownerDisplayName;

    // Reports associated with this FanHub
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
