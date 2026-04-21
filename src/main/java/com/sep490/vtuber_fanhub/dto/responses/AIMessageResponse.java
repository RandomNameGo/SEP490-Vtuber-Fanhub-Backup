package com.sep490.vtuber_fanhub.dto.responses;

import com.sep490.vtuber_fanhub.models.ChatMessageMetadata;
import com.sep490.vtuber_fanhub.models.Enum.MetadataType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AIMessageResponse {
    private String message;
    private String thought;
    private boolean hasMetadata;
    private MetadataType metadataType;
    private Long metadataTargetId;
}
