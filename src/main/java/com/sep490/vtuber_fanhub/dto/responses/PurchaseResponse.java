package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class PurchaseResponse {

    private Long userItemId;
    private Long userId;
    private Long itemId;
    private String itemName;
    private Long price;
    private Instant purchasedAt;
}
