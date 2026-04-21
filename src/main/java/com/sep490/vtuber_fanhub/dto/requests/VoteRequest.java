package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {

    @NotNull(message = "Post ID must not be null")
    private Long postId;

    @NotNull(message = "Option ID must not be null")
    private Long optionId;
}
