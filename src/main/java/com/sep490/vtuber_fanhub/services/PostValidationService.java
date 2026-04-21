package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Post;

public interface PostValidationService {
    void validatePost(Post post);

    default void handleVideoCallback(JsonNode jsonNode) {
    }
    // this method is purely for async implementation of this interface.
    // this method is called when all media validation is done.
    default void finalizeValidation(Post post) {
    }
}
