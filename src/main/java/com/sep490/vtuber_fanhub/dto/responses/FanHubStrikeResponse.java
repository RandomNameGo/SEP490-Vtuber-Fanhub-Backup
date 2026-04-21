package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;
import java.time.Instant;

@Data
public class FanHubStrikeResponse {
    private Long id;
    private Long fanHubId;
    private String fanHubName;
    private String reason;
    private Long strikeBySystemAccountId;
    private String strikeByUsername;
    private Instant createdAt;
    private Boolean isActive;
}
