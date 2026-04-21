package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class BannerResponse {

    private Long bannerId;
    private String name;
    private Instant startTime;
    private Instant endTime;
    private String description;
    private Integer gachaCost;
    private Instant createdAt;
    private String bannerImgUrl;
    private Boolean isActive;
}
