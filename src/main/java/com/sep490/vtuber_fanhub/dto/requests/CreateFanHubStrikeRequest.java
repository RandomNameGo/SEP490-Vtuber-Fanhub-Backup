package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

@Data
public class CreateFanHubStrikeRequest {
    private Long fanHubId;
    private String reason;
}
