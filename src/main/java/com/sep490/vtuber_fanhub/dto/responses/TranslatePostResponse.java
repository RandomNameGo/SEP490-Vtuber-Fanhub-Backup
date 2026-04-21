package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslatePostResponse {
    private String translatedContent;
    private String translatedTitle;
    private boolean translateLanguageSet;
    private String extraComment;
}
