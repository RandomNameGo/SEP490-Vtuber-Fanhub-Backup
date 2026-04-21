package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SystemAnalyticResponse {
    private Map<String, Long> userCountByRole;
    private long totalFanHub;
    private List<FanHubResponse> top5FanHubs;
    private long newUsersInWeek;
    private List<UserResponse> topVtubersByOshi;
    private List<String> trendingHashtags;
}
