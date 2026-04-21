package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

@Data
public class ItemResponse {
    private Long id;
    private String itemName;
    private String description;
    private String imageUrl;
    private String category;
}
