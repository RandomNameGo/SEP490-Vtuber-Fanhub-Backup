package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class VTuberApplicationResponse {

    private Long id;

    private Long userId;
    private String username;

    private String channelName;
    private String channelLink;
    private String status;
    private String reason;

    private Long reviewerId;
    private String reviewerUsername;

    private Instant createdAt;
    private Instant reviewAt;
}
