package com.sep490.vtuber_fanhub.dto.responses;

import com.sep490.vtuber_fanhub.models.Enum.MetadataType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetadataResponse {
    MetadataType metadataType;
    Long postId;
    String postTitle;
    String postContent;
    String imagePreviewUrl;
}
