package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class FanHubAnalyticsResponse {
    private long totalJoinedMembers;
    private long totalPosts;
    private long totalStrikes;
    private List<FanHubMemberResponse> topMembers;
    private List<StrikeDetails> strikes;

    @Data
    @Builder
    public static class StrikeDetails {
        private String reason;
        private Instant createdAt;
    }
}
