package com.sep490.vtuber_fanhub.dto.requests;

import lombok.Data;

@Data
public class CreateReportPostRequest {

    private Long postId;

    private String reason;
}
