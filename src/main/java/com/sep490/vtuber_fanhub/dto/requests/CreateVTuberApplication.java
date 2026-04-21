package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVTuberApplication {
    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotBlank(message = "Channel name must not be blank")
    @Size(max = 50, message = "Channel name must not exceed 50 characters")
    private String channelName;

    @NotBlank(message = "Channel link must not be blank")
    @Size(max = 512, message = "Channel link must not exceed 512 characters")
    // @URL(message = "Channel link must be a valid URL")
    private String channelLink;
}
