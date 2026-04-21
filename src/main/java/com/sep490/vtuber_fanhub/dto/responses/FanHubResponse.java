package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class FanHubResponse {

    private Long fanHubId;
    private String subdomain;
    private String hubName;
    private String description;
    private String bannerUrl;
    private List<String> highlightImgUrls;
    private String backgroundUrl;
    private String themeColor;
    private String avatarUrl;
    private Boolean isPrivate;
    private Boolean requiresApproval;
    private Instant createdAt;

    private Long ownerUserId;
    private String ownerUsername;
    private String ownerDisplayName;

    private List<String> categories;

    private Long memberCount;
}
