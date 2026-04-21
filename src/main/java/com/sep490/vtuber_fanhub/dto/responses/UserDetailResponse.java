package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class UserDetailResponse {
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String frameUrl;
    private String bio;
    private String role;
    private Long points;
    private Long paidPoints;
    private String translateLanguage;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isActive;

    private OshiResponse oshi;

    @Data
    public static class OshiResponse {
        private Long userId;
        private String username;
        private String displayName;
        private String avatarUrl;
    }
}
