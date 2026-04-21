package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Enum.PostMediaType;
import org.springframework.web.multipart.MultipartFile;

public interface SightEngineService {
    JsonNode checkMediaFile(MultipartFile file, PostMediaType mediaType);
    JsonNode checkMediaUrl(String url, PostMediaType mediaType);
    JsonNode checkVideoUrlAsync(String url);
}
