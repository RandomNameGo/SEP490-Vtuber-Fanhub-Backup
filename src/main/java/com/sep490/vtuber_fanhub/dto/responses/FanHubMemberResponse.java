package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class FanHubMemberResponse {

    private Long id;

    private Long hubId;
    private String hubName;

    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;

    private String roleInHub;
    private String status;
    private Integer fanHubScore;
    private Instant joinedAt;
    private String title;
}
