package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBannerItemRequest {

    @NotNull(message = "Banner ID must not be null")
    private Long bannerId;

    private Long itemId;

    @Size(max = 100, message = "Item name must not exceed 100 characters")
    private String itemName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @NotNull(message = "Multiplier must not be null")
    private Integer multiplier;

    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;
}
