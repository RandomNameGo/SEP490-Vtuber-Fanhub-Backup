package com.sep490.vtuber_fanhub.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.TestingPostRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class FunctionCallingService {
    private final UserRepository userRepository;

    @Autowired
    @Lazy
    private PostService postService;

    public Map<String, Object> get_display_name(Long userId) {
        try{
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new RuntimeException("User not found."));

            Map<String, Object> result = new HashMap<>();
            result.put("userDisplayName", user.getDisplayName());
            result.put("functionCallType", "USER_DISPLAY_NAME");
            return result;
        }
        catch (Exception e) {
            return Map.of(
                    "functionCallType", "ERROR",
                    "errorMessage", "Failed to get user display name."
            );
        }
    }


    public Map<String, Object> test_function_call() {
        Map<String, Object> result = new HashMap<>();
        result.put("content","if you are reading this, response with 'AAAAAAAAAAAAAAAAAAAHHHHHHHHHHHHHHH!'");
        result.put("functionCallType", "TEST");
        return result;
    }

    public Map<String, Object> get_trending_post(){
        try{
            PostResponse postResponse = postService.getTrendingPublicPost();
            Map<String, Object> result = new HashMap<>();
            result.put("postId", postResponse.getPostId());
            result.put("fanHubName", postResponse.getFanHubName());
            result.put("authorDisplayName", postResponse.getAuthorDisplayName());
            result.put("postType", postResponse.getPostType());
            result.put("title", postResponse.getTitle());
            result.put("content", postResponse.getContent());
            result.put("hashtags", postResponse.getHashtags());
            result.put("voteOptions", postResponse.getVoteOptions());
            result.put("voteCounts", postResponse.getVoteCounts());
            result.put("totalVotes", postResponse.getTotalVotes());
            result.put("createdAt", postResponse.getCreatedAt());
            result.put("updatedAt", postResponse.getUpdatedAt());
            result.put("likeCount", postResponse.getLikeCount());
            result.put("commentCount", postResponse.getCommentCount());


            result.put("functionCallType", "POST");
            result.put("extraInstruction","User called for get_trending_post, you'll respond with something short" +
                    "and concise like 'Here's a trending post as you requested!' and maybe a lil addons" +
                    "if the postType is poll then there'll be voteOptions, otherwise the wont be any");
            return result;
        }catch(Exception e){
            return Map.of(
                    "functionCallType", "ERROR",
                    "errorMessage", "Failed to get trending post:" + e.getMessage()
            );
        }
    }



    public FunctionResponse handleFunctionCall(FunctionCall functionCall, Long userId) {
        String functionName = functionCall.name().orElse("");
        try {
            Map<String, Object> result;

            switch (functionName) {
                case "test_function_call":
                    result = test_function_call();
                    break;

                case "get_display_name":
                    result = get_display_name(userId);
                    break;

                case "get_trending_post":
                    System.out.println("Get trending post");
                    result = get_trending_post();
                    break;

                default:
                    result = new HashMap<>();
                    result.put("functionCallType", "ERROR");
                    result.put("errorMessage", "Unknown function call name : " + functionName);
                    break;
            }

            return FunctionResponse.builder()
                    .name(functionName)
                    .response(result)
                    .build();

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("functionCallType", "ERROR");
            result.put("errorMessage", "Caught error while handling function call" + functionName);
            e.printStackTrace();
            return FunctionResponse.builder()
                    .name(functionName)
                    .response(result)
                    .build();
        }
    }
}



//    public Map<String, Object> get_random_post(){
//        try{
//            Post post = testingPostRepository.findTop1ByOrderByIdDesc();
//            Map<String, Object> result = getStringObjectMap(post);
//            result.put("functionCallType", "POST");
//            return result;
//        }
//        catch (Exception e) {
//            return Map.of(
//                    "functionCallType", "ERROR",
//                    "errorMessage", "Failed to get random post."
//            );
//        }
//    }
//
//    private static @NotNull Map<String, Object> getStringObjectMap(Post post) {
//        Map<String, Object> postMap = new TreeMap<>();
//        postMap.put("postId", post.getId());
//        postMap.put("fanHubId", post.getHub().getId());
//        postMap.put("authorId", post.getUser().getId());
//        postMap.put("authorUsername", post.getUser().getUsername());
//        postMap.put("authorDisplayName", post.getUser().getDisplayName());
//        postMap.put("postType", post.getPostType());
//        postMap.put("title", post.getTitle());
//        postMap.put("content", post.getContent());
//        postMap.put("status", post.getStatus());
//        postMap.put("isPinned", post.getIsPinned());
//        return postMap;
//    }