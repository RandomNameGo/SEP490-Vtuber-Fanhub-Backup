package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class UserItemResponse {

    private Long userItemId;
    private Long itemId;
    private String itemName;
    private String description;
    private String imageUrl;
    private String category;
    private Instant obtainedAt;
}
