package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseItemRequest {

    @NotNull(message = "Shop item ID must not be null")
    private Long shopItemId;
}
