package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

@Data
public class CreateReportMemberRequest {

    private Long memberId;

    private String reason;

    private Long relatedCommentId;
}
