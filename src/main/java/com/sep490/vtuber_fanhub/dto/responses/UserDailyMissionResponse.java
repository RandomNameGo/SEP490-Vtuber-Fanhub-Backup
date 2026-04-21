package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

@Data
public class UserDailyMissionResponse {
    private Integer likeAmount;
    private Boolean bonus10;
    private Boolean bonus20;
}
