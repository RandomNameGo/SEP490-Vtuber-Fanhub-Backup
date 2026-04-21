package com.sep490.vtuber_fanhub.dto.responses;


import com.sep490.vtuber_fanhub.models.Enum.MetadataType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MessageResponse {
    Long id;
    Instant createdAt;
    String content;
    String senderRole;
    String thought;
    MetadataResponse metadataResponse;
}
