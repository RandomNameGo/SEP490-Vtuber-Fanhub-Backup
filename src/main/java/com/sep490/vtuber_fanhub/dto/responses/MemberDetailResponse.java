package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class MemberDetailResponse {

    // FanHubMember fields
    private Long memberId;
    private Long hubId;
    private String hubName;
    private String roleInHub;
    private String status;
    private Integer fanHubScore;
    private Instant joinedAt;
    private String title;

    // User fields
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String frameUrl;
}
