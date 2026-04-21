package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class PostResponse {

    private Long postId;
    private Long fanHubId;
    private String fanHubName;
    private String fanHubSubdomain;

    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;

    private String postType;
    private String title;
    private String content;
    private String status;
    private Boolean isPinned;

    private List<String> mediaUrls;
    private List<String> hashtags;
    private List<VoteOptionResponse> voteOptions;
    private Map<Long, Long> voteCounts;
    private Long totalVotes;
    private Long userVotedOptionId;

    private Instant createdAt;
    private Instant updatedAt;

    private Long likeCount;
    private Boolean isLikedByCurrentUser;

    private Long commentCount;
}
