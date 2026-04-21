package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class BanMemberResponse {

    private Long banId;

    private Long fanHubId;
    private String fanHubName;

    private Long userId;
    private String username;
    private String displayName;

    private Long bannedByUserId;
    private String bannedByUsername;
    private String bannedByDisplayName;

    private String reason;
    private String banType;
    private Instant bannedUntil;
    private Boolean isActive;

    private Instant createdAt;
}
