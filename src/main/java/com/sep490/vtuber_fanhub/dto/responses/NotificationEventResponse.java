package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;


@Data
@Builder
public class NotificationEventResponse {
    

    private Long id;
    

    private String type;
    

    private String title;
    

    private String message;
    

    private Long relatedHubId;
    

    private String relatedHubName;
    

    private Long relatedPostId;
    

    private String relatedPostTitle;
    

    private Long triggeredByUserId;
    

    private String triggeredByUsername;
    

    private String triggeredByAvatarUrl;
    

    private Boolean isRead;


    private Instant createdAt;
}
