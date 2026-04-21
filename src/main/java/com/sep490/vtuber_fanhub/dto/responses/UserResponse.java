package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String frameUrl;
    private String bio;
    private String role;
    private Long points;
    private Long paidPoints;
    private String translateLanguage;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isActive;

    private Long totalBadges;
    private Long totalFanHubs;
    private Long totalReceivedGifts;

    private List<UserDisplayBadgeResponse> displayBadges;

    private List<UserAllBadgeResponse> allBadges;

    private List<UserHubJoinedResponse> fanHubsJoined;

    private OshiResponse oshi;


    @Data
    public static class UserDisplayBadgeResponse {
        private Long userBadgeId;
        private Long badgeId;
        private String badgeName;
        private String description;
        private String iconUrl;
        private String requirement;
        private Instant acquiredAt;
        private Boolean isDisplay;
    }

    @Data
    public static class UserAllBadgeResponse {
        private Long userBadgeId;
        private Long badgeId;
        private String badgeName;
        private String description;
        private String iconUrl;
        private String requirement;
        private Instant acquiredAt;
        private Boolean isDisplay;
    }

    @Data
    public static class UserHubJoinedResponse {
        private Long fanHubId;
        private String subdomain;
        private String hubName;
        private String themeColor;
        private String avatarUrl;
    }

    @Data
    public static class OshiResponse {
        private Long userId;
        private String username;
        private String displayName;
        private String avatarUrl;
    }
}
