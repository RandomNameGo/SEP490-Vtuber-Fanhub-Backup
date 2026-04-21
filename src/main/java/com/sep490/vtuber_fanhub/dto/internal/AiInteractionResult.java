package com.sep490.vtuber_fanhub.dto.internal;

import com.google.genai.types.GenerateContentResponse;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiInteractionResult {
    private GenerateContentResponse response;
    private List<Map<String, Object>> metadataList;
    public AiInteractionResult(GenerateContentResponse response, List<Map<String, Object>> metadataList) {
        this.response = response;
        this.metadataList = metadataList;
    }
}
