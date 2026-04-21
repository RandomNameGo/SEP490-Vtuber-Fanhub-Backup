package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.types.GenerateContentResponse;
import com.sep490.vtuber_fanhub.dto.internal.AiInteractionResult;
import com.sep490.vtuber_fanhub.dto.responses.AIMessageResponse;
import com.sep490.vtuber_fanhub.models.Enum.ChatPersonalityType;

public interface GeminiAIService {
    String test();
    AIMessageResponse sendPrompt(String prompt, ChatPersonalityType type);
    JsonNode listModels();
    AiInteractionResult sendPromptFullResponse(String prompt, ChatPersonalityType type);
    String translatePost(String content, String title, String language);
    String summarizePost(String content, String title, String language);

    // these are for function calling of chatbot, which require userid for authentication
    AIMessageResponse sendPromptFunctionCalling(String prompt, ChatPersonalityType type, Long userId);
    AiInteractionResult sendPromptFunctionCallingFullResponse(String prompt, ChatPersonalityType type, Long userId);
}
