package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Post;
import org.springframework.web.multipart.MultipartFile;

public interface ContentValidationService {
    String validatePostContent(String title, String content);
    String validateImageFile(MultipartFile file);
    String validateImageUrl(String url);
    String validateVideoUrl(String url);
    JsonNode validateVideoUrlAsync(String url);
    String handleCallbackResult(JsonNode result);
    String validatePostPollOptions(Post post);
}
