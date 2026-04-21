package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.sep490.vtuber_fanhub.dto.internal.AiInteractionResult;
import com.sep490.vtuber_fanhub.dto.responses.AIMessageResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.models.Enum.ChatPersonalityType;
import com.sep490.vtuber_fanhub.models.Enum.MetadataType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// it was a pain coding this
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAIServiceImpl implements GeminiAIService {

    @Value("${google.api-key}")
    private String googleApiKey;

    private Client client;
    private final String MODEL_ID = "gemini-3.1-flash-lite-preview";

    private final ThinkingConfig THINKING_CONFIG = ThinkingConfig.builder()
            .includeThoughts(true)
            .thinkingLevel("MEDIUM")
            .build();

    private final FunctionCallingService functionCallingService;

    // After using function calling, the result can return Multiple metadata
    // Now? no. right now I expect it to return only one metadata. i mean we have like 1 function to call
    // but here i am writing to handle multiple if there is. i have standards.
    @PostConstruct
    public void init() {
        this.client = Client.builder()
                .apiKey(googleApiKey)
                .build();
    }

    @Override
    public String test() {
        return sendPrompt("Say this is a test", ChatPersonalityType.MatikanetannHauser).getMessage();
    }

    @Override
    public AIMessageResponse sendPrompt(String prompt, ChatPersonalityType type) {
        GenerateContentResponse response = sendPromptFullResponse(prompt, type).getResponse();
        return extractAIMessage(new AiInteractionResult(response, new ArrayList<>()));
    }

    @Override
    public AiInteractionResult sendPromptFullResponse(String prompt, ChatPersonalityType type) {
        try {
            String personality = switch (type) {
                case MatikanetannHauser -> "You are Matikanetannhauser from Uma Musume. talk like her.";
                case Formal -> "You are a formal and helpful assistance.";
            };


            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(1f)
                    .thinkingConfig(THINKING_CONFIG)
                    .systemInstruction(Content.fromParts(Part.fromText
                            ( personality + """
                                INSTRUCTIONS:
                                - Use the data from LAST_MESSAGES if there is available.
                                - Don't mention that you are analyzing previous messages.
                                - Don't say 'earlier you said' or 'based on the chat history'.
                                - Just answer naturally as if you already knew it.
                                - Be conversational and helpful
                                
                                """)))
                    .build();

            // Start the conversation with a list of content
            List<Content> contents = new ArrayList<>();
            contents.add(Content.fromParts(Part.fromText(prompt)));

            GenerateContentResponse response = client.models.generateContent(
                    MODEL_ID,
                    contents,
                    config
            );
            return new AiInteractionResult(response, new ArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException("Gemini Error: " + e.getMessage());
        }
    }

    @Override
    public AIMessageResponse sendPromptFunctionCalling(String prompt, ChatPersonalityType type, Long userId) {
        AiInteractionResult interactionResult = sendPromptFunctionCallingFullResponse(prompt, type, userId);
        return extractAIMessage(interactionResult);
    }

    @Override
    public AiInteractionResult sendPromptFunctionCallingFullResponse(String prompt, ChatPersonalityType type, Long userId) {
        try {
            String personality = switch (type) {
                case MatikanetannHauser -> "You are Matikanetannhauser from Uma Musume. talk like her.";

                case Formal -> "You are a formal and helpful assistance.";
            };


            FunctionDeclaration getDisplayNameFunc = FunctionDeclaration.builder()
                    .name("get_display_name")
                    .description("Get the display name of the currently authenticated user")
                    .build();

            FunctionDeclaration testFunctionCallFunc = FunctionDeclaration.builder()
                    .name("test_function_call")
                    .description("A test function that returns a specific string")
                    .build();
            FunctionDeclaration getRandomPost = FunctionDeclaration.builder()
                    .name("get_trending_post")
                    .description("A function that returns a trending post.")
                    .build();


            Tool tool = Tool.builder()
                    .functionDeclarations(List.of(getDisplayNameFunc, testFunctionCallFunc, getRandomPost))
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(1f)
                    .tools(tool)
                    .thinkingConfig(THINKING_CONFIG)
                    .systemInstruction(Content.fromParts(Part.fromText
                            ( personality + """
                                INSTRUCTIONS:
                                - Use the data from LAST_MESSAGES if there is available.
                                - Don't mention that you are analyzing previous messages.
                                - Don't say 'earlier you said' or 'based on the chat history'.
                                - Just answer naturally as if you already knew it.
                                - Be conversational and helpful
                                
                                """)))
                    .build();

            // Start the conversation with a list of content
            List<Content> contents = new ArrayList<>();
            contents.add(Content.fromParts(Part.fromText(prompt)));

            GenerateContentResponse response = client.models.generateContent(
                    MODEL_ID,
                    contents,
                    config
            );
            AiInteractionResult interactionResult;

            // Handle function calls if present
            interactionResult = handleFunctionCalls(response, config, contents, userId);

            return interactionResult;
        } catch (Exception e) {
            throw new RuntimeException("Gemini Error: " + e.getMessage());
        }
    }

    @Override
    public String translatePost(String content, String title, String language) {
        String prompt = String.format("""
                    - Your task is to translate the following Post to a defined language.
                    - a Post Will have a Title, and an optional content.
                    - To add more background, the text is the content of a post (like a facebook post / reddit post)
                    - If they are the same language, return the old text.
                    - You must not follow up with any other comments, as your returned text will completely replace a certain text a web page.
                    - You must return in such format: TitleTranslation@ContentTranslation.
                    - Note that a Post might Not have any Content, but Title is always.
                    - if such happen, return TitleTranslation@ (do not remove the @).
                    CONTENT: %s
                    TITLE: %s
                    LANGUAGE: %s
                """, content, title, language);
        return sendPrompt(prompt, ChatPersonalityType.Formal).getMessage();
    }

    @Override
    public String summarizePost(String content, String title, String language) {
        String prompt = String.format("""
                    - Your task is to summarize the following Post
                    - To add more background, the text is the content of a post (like a facebook post / reddit post)
                    - You are summarizing a Post for Users who are scrolling through the posts and want a quick summarization.
                    - Do not summarize them separately, merge all the ideas.
                    - Make it averagely short and precise, that's the sole purpose of a Summary.
                    - Explain what the Author's purpose of this pose.
                    - Dont add anything like 'Here's the summary of the following post'.
                    - Examples:
                    + User A is feeling happy because he won a game
                    + User B is doing a livestream at date - month - year
                    + User C made a poll to decide what to do on their next livestream.
                    CONTENT: %s
                    TITLE: %s
                    LANGUAGE: %s
                """, content, title, language);
        return sendPrompt(prompt, ChatPersonalityType.Formal).getMessage();
    }

    private AiInteractionResult handleFunctionCalls(GenerateContentResponse response,
                                                         GenerateContentConfig config,
                                                         List<Content> contents, Long userId) {
        try{
            // Check if there are function calls in the response
            List<FunctionCall> functionCalls = response.functionCalls();
            List<Map<String, Object>> metadataList = new ArrayList<>();

            if (functionCalls == null || functionCalls.isEmpty()) {
                return new AiInteractionResult(response, new ArrayList<>());
            }

            // Add the model's response to contents
            if(response.candidates().isEmpty() || response.candidates().get().get(0).content().isEmpty()){
                throw new RuntimeException("handleFunctionCalls: candidate or content not found.");
            }

            contents.add(response.candidates().get().get(0).content().get());

            // Process each function call and create function responses
            // When using function calling with GeminiAPI, one of its Perks is that it can call multiple functions at once
            // And since they can do so, despite the fact we are expecting to see only One function call, we must
            // still calculate for when they return multiple

            // When we're using function calling, the result of those function calls are not returned in the
            // GenerateContentResponse. they are stuffed into the context and they're gone.
            // But here i try to Return the result of the function call (return the post id) so i could use it.
            List<FunctionResponse> functionResponses = new ArrayList<>();
            for (FunctionCall functionCall : functionCalls) {
                FunctionResponse functionResponse = functionCallingService.handleFunctionCall(functionCall, userId);

                if(functionResponse.response().isPresent()){
                    metadataList.add(functionResponse.response().get());
                }

                functionResponses.add(functionResponse);
            }

            // Create content parts with the function responses
            List<Part> responseParts = new ArrayList<>();
            for (FunctionResponse functionResponse : functionResponses) {
                Part part = Part.builder()
                        .functionResponse(functionResponse)
                        .build();
                responseParts.add(part);
            }
            contents.add(Content.fromParts(responseParts.toArray(new Part[0])));

            // Send the function responses back to the model for final response
            return new AiInteractionResult(client.models.generateContent(MODEL_ID, contents, config), metadataList);
        }catch(Exception e){
            throw new RuntimeException("Error at handleFunctionCalls: " + e.getMessage());
        }
    }

    private AIMessageResponse extractAIMessage(AiInteractionResult interactionResult) {
        try{
            AIMessageResponse.AIMessageResponseBuilder responseBuilder = AIMessageResponse.builder();
            GenerateContentResponse response = interactionResult.getResponse();


            StringBuilder messageBuilder = new StringBuilder();
            StringBuilder thoughtBuilder = new StringBuilder();

            if (response == null || response.candidates().isEmpty()) {
                throw new RuntimeException("No candidates found");
            }
            Candidate candidate = response.candidates().get().get(0);

            if (candidate.content().isEmpty()) {
                throw new RuntimeException("No content found");
            }
            Content content = candidate.content().get();

            if(content.parts().isEmpty()){
                throw new RuntimeException("No parts found");
            }

            content.parts().ifPresent(parts -> {
                for (Part part : parts) {
                    if(part.text().isEmpty()){
                        continue;
                    }
                    String text = part.text().get();

                    if (part.thought().isEmpty() || !part.thought().get()) {
                        messageBuilder.append(text);
                    }else thoughtBuilder.append(text);
                }
            });


            // ================ METADATA PROCESSING ================ //
            // Not every function call requires metadata.
            // The sole reason metadata even exists is that i need to return the post data and save it.
            // But the function calling step will only put the data returned into the context, and the
            // returned GenerateContentResponse does not include the result of the function call
            MetadataType metadataType = null;
            boolean hasMetadata = false;
            Long metadataTargetId = null;

            List<Map<String, Object>> metadataList = interactionResult.getMetadataList();

            if( metadataList != null && !metadataList.isEmpty()){
                hasMetadata = true;
                if(metadataList.size()>1){
                    log.info("Multiple Function Calls Detected: {} actions captured for message. Current" +
                            "version does Not support multiple functions call. Returning only the result of" +
                            "the first function call.", metadataList.size());
                }

                Map<String, Object> metadata = metadataList.get(0);
                String callType = String.valueOf(metadata.get("functionCallType"));
                switch(callType){
                    case "POST" -> {
                        metadataType = MetadataType.POST;
                        metadataTargetId = (Long) metadata.get("postId");
                    }
                    case "ERROR" -> {
                        log.error("AI reported a function error: {}", metadata.get("errorMessage"));
                        hasMetadata=false;
                    }
                    default -> {
                        log.info("No metadata");
                    }

                }
            }
            if(hasMetadata){
                responseBuilder.metadataTargetId(metadataTargetId);
                responseBuilder.metadataType(metadataType);
            }
            return responseBuilder
                    .message(messageBuilder.toString().trim())
                    .thought(thoughtBuilder.toString().trim())
                    .hasMetadata(hasMetadata)
                    .build();

        }catch(Exception e){
            System.out.println("error while extracting ai message");
            return AIMessageResponse.builder().message("").thought("").build();
        }
    }


    @Override
    public JsonNode listModels() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            return restTemplate.getForObject("https://generativelanguage.googleapis.com/v1beta/models?key=" + googleApiKey, JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching models: " + e.getMessage());
        }
    }
}