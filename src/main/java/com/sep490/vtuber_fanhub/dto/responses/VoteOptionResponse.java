package com.sep490.vtuber_fanhub.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteOptionResponse {
    private Long id;
    private String optionText;
}
