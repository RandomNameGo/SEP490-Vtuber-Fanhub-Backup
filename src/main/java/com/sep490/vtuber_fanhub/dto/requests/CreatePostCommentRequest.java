package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostCommentRequest {

    @NotNull(message = "Post ID must not be null")
    private Long postId;

    private Long parentCommentId;

    @NotBlank(message = "Content must not be blank")
    //@Size(max = 1000, message = "Content must not exceed 1000 characters")
    private String content;
}
