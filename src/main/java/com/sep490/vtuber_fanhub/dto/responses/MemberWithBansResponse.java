package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class MemberWithBansResponse {

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

    // Bans associated with this member
    private List<SimpleMemberBanResponse> bans;

    @Data
    public static class SimpleMemberBanResponse {
        private Long banId;
        private Long bannedByUserId;
        private String bannedByUsername;
        private String bannedByDisplayName;
        private String reason;
        private String banType;
        private Instant bannedUntil;
        private Boolean isActive;
        private Instant createdAt;
    }
}
