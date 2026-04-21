package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class GachaResultResponse {

    private Long userItemId;
    private Long userId;
    private Long itemId;
    private String itemName;
    private String imageUrl;
    private Integer multiplier;
    private String type;
    private Integer cost;
    private Integer pointsRefunded;
    private Instant obtainedAt;
}
