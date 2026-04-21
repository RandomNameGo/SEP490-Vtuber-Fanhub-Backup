package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GachaBannerItemRequest {

    @NotNull(message = "Banner ID must not be null")
    private Long bannerId;
}
