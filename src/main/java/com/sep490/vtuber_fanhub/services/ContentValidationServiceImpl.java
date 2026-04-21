package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sep490.vtuber_fanhub.models.Enum.ChatPersonalityType;
import com.sep490.vtuber_fanhub.models.Enum.PostMediaType;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.VoteOption;
import com.sep490.vtuber_fanhub.repositories.VoteOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentValidationServiceImpl implements ContentValidationService{
    private final GeminiAIService geminiAIService;
    private final SightEngineService sightEngineService;
    private final VoteOptionRepository voteOptionRepository;

    @Override
    public String validatePostContent(String title, String content) {
        String intentPrompt = String.format("""
            Your task is to validate the following content from a post.
            A post contains of title, and content.
            
            Text must not contain any inappropriate languages, hate, or discrimination
            But at the same time, dont be too strict.

            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT: Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"

            Example: This post's content is safe@AI_SAFE
            Example: this post's content is not safe. bad keywords found are: ... know what, *Fuck* you an...@AI_UNSAFE

            Title: "%s"
            Content: "%s"

            """, title, content);

        return geminiAIService.sendPrompt(intentPrompt, ChatPersonalityType.Formal).getMessage();
    }

    @Override
    public String validateImageFile(MultipartFile file) {
        JsonNode mediaValidationResult = sightEngineService.checkMediaFile(file, PostMediaType.IMAGE);
        String intentPrompt = String.format("""
            Your task is to provide your comment based on the following result of SightEngine


            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT:Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"

            Example: This image is safe@AI_SAFE
            Example: this image is not safe. based on data received, this image is gore-y.@AI_UNSAFE

            Text: "%s"

            """, mediaValidationResult.toString());
        return geminiAIService.sendPrompt(intentPrompt, ChatPersonalityType.Formal).getMessage();
    }

    @Override
    public String validateImageUrl(String url) {
        JsonNode mediaValidationResult = sightEngineService.checkMediaUrl(url, PostMediaType.IMAGE);
        String intentPrompt = String.format("""
            Your task is to provide your comment based on the following result of SightEngine


            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT: Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"

            Example: This image is safe@AI_SAFE
            Example: this image is not safe. based on data received, this image is gore-y.@AI_UNSAFE

            SightEngine Result: "%s"

            """, mediaValidationResult.toString());
        return geminiAIService.sendPrompt(intentPrompt, ChatPersonalityType.Formal).getMessage();
    }

    @Override
    public String validateVideoUrl(String url) {
        JsonNode mediaValidationResult = sightEngineService.checkMediaUrl(url, PostMediaType.VIDEO);
        String intentPrompt = String.format("""
            Your task is to provide your comment based on the following result of SightEngine


            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT: Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"

            Example: This video is safe@AI_SAFE
            Example: this video is not safe. based on data received, this image is gore-y.@AI_UNSAFE

            SightEngine Result: "%s"

            """, mediaValidationResult.toString());
        return geminiAIService.sendPrompt(intentPrompt, ChatPersonalityType.Formal).getMessage();
    }

    @Override
    public JsonNode validateVideoUrlAsync(String url) {
        return sightEngineService.checkVideoUrlAsync(url);
    }

    @Override
    public String handleCallbackResult(JsonNode result) {
        String intentPrompt = String.format("""
            Your task is to provide your comment based on the following result of SightEngine


            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT: Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"

            Example: This video is safe@AI_SAFE
            Example: this video is not safe. based on data received, this image is gore-y.@AI_UNSAFE

            SightEngine Result: "%s"

            """, result.toString());
        return geminiAIService.sendPrompt(intentPrompt, ChatPersonalityType.Formal).getMessage();
    }

    @Override
    public String validatePostPollOptions(Post post) {
        List<VoteOption> voteOptions = voteOptionRepository.findAllByPostId(post.getId());
        String[] voteOptionStrings = {"", "", "", ""};
        for(int i = 0; i <voteOptions.size(); i++ ){
            voteOptionStrings[i] = voteOptions.get(i).getOptionText();
        }
        String intentPrompt = String.format("""
            Your task is to validate each of the poll options.
            A post may have up to 4 poll options, minimum is 2.

            Poll option text must not contain any inappropriate languages, hate, or discrimination
            But at the same time, dont be too strict.

            The respond Must Not be in quotes. Only text.
            Keep your answer as short as possible, while maintaining all key points.
            !!!IMPORTANT: Your answer must be in format: "comment@status"
            !!!IMPORTANT: status must be either "AI_SAFE" or "AI_UNSAFE"
            example: All options' content are safe.@AI_SAFE
            example: At Option 1, user said "...".@AI_UNSAFE

            Option 1: "%s"
            Option 2: "%s"
            Option 3: "%s"
            Option 4: "%s"

            """, voteOptionStrings[0], voteOptionStrings[1], voteOptionStrings[2], voteOptionStrings[3]);
        return geminiAIService.sendPrompt(intentPrompt, ChatPersonalityType.Formal).getMessage();
    }
}
