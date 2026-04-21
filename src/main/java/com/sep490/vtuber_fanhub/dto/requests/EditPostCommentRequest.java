package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditPostCommentRequest {

    @NotBlank(message = "Content must not be blank")
    private String content;
}
