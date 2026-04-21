package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

@Data
public class FanHubMembershipResponse {
    private Boolean isMember;
    private String roleInHub;
}
