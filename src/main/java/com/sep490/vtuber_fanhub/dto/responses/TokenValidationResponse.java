package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TokenValidationResponse {
    private boolean valid;
    private boolean expired;
    private Long userId;
    private String username;
    private String role;
    private Instant expiresAt;
}
