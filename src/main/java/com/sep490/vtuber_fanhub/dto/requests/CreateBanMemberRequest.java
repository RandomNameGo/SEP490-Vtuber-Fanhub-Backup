package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

import java.time.Instant;

@Data
public class CreateBanMemberRequest {

    private Long fanHubMemberId;

    private String reason;

    private String banType;

    private Instant bannedUntil;
}
