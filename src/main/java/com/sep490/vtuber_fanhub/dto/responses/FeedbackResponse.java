package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class FeedbackResponse {

    private Long feedbackId;
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private String username;
    private String displayName;
    private String content;
    private Instant createdAt;
    private Boolean isRead;
}
