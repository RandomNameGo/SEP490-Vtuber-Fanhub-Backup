package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateItemRequest {

    @NotBlank(message = "Item name must not be blank")
    @Size(max = 100, message = "Item name must not exceed 100 characters")
    private String itemName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
}
