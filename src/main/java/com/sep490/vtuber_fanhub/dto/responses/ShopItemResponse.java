package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

@Data
public class ShopItemResponse {

    private Long shopItemId;
    private Long itemId;
    private String itemName;
    private String description;
    private String imageUrl;
    private String category;
    private Long price;
}
