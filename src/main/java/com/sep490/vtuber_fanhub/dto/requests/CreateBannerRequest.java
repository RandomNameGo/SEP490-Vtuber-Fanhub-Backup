package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateBannerRequest {

    @NotBlank(message = "Banner name must not be blank")
    @Size(max = 512, message = "Banner name must not exceed 512 characters")
    private String name;

    @NotNull(message = "Start time must not be null")
    private Instant startTime;

    @NotNull(message = "End time must not be null")
    private Instant endTime;

    @Size(max = 65535, message = "Description must not exceed 65535 characters")
    private String description;

    private Integer gachaCost;
}
