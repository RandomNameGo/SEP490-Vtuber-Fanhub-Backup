package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

@Data
public class BannerItemResponse {

    private Long bannerItemId;
    private Long bannerId;
    private Long itemId;
    private String itemName;
    private String description;
    private String imageUrl;
    private String category;
    private Integer multiplier;
    private String type;
}
