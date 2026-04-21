package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

import java.util.List;

@Data
public class SelectUserBadgeRequest {
    private List<Long> userBadgeIds;
}
