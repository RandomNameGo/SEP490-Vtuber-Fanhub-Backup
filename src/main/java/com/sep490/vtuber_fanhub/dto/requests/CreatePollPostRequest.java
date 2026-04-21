package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreatePollPostRequest {

    @NotNull(message = "FanHub ID must not be null")
    private Long fanHubId;

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @NotNull(message = "Poll options must not be null")
    @Size(min = 2, max = 4, message = "Poll must have between 2 and 4 options")
    private List<@NotBlank(message = "Option text must not be blank") 
                 @Size(max = 255, message = "Option text must not exceed 255 characters") String> options;

    @Size(max = 5, message = "Cannot exceed 5 hashtags")
    private List<String> hashtags;
}
