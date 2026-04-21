package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetOshiRequest {

    @NotBlank(message = "Oshi username is required")
    private String oshiUsername;
}
