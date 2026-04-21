package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostMedia;
import com.sep490.vtuber_fanhub.repositories.PostMediaRepository;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

// This is the Synchronous implementation of PostValidationService
// By synchronous, I meant for AI Video Validation, I use the Synchronous Approach.
// But we can only validate short videos, which are under 60s
// To validate longer videos, we must use the Asynchronous Approach,
// which I made the Asynchronous implementation for
// The reason why I made two separate files is that it gets kinda tricky to solve the logic
// Where the Image API is synchronous, while the Video API is asynchronous.
// This could work as a backup if the asynchronous implementation is not working well.
@Service("postValidationServiceImplSync")
@RequiredArgsConstructor
public class PostValidationServiceImplSync implements PostValidationService {

    private final PostRepository postRepository;
    private final PostMediaRepository mediaRepository;
    private final ContentValidationService contentValidationService;

    @Override
    @Async("validationExecutor")
    public void validatePost(Post post) {
        try {
            StringBuilder totalComments = new StringBuilder();

            boolean isSafe = true;

            if(post.getPostType().equals("IMAGE") || post.getPostType().equals("VIDEO")) {
                List<PostMedia> postMediaList = mediaRepository.findByPostId(post.getId());
                boolean isVideo = post.getPostType().equals("VIDEO");
                for(PostMedia postMedia : postMediaList) {
                    String ai_validation;
                    if(isVideo){
                        ai_validation = contentValidationService.validateVideoUrl(postMedia.getMediaUrl());
                    }else{
                        ai_validation = contentValidationService.validateImageUrl(postMedia.getMediaUrl());
                    }
                    String[] media_validation_split = ai_validation.split("@");
                    if(media_validation_split.length<2){
                        throw new RuntimeException("AI returned incorrect form");
                    }
                    postMedia.setAiValidationComment(media_validation_split[0]);
                    postMedia.setAiValidationStatus(media_validation_split[1]);
                    mediaRepository.save(postMedia);
                    if(media_validation_split[1].equals("AI_UNSAFE")) isSafe = false;
                }
                if(!isSafe){
                    totalComments.append("Some medias are found not safe.");
                }else totalComments.append("All medias are found safe.");
            }else{
                throw new RuntimeException("Unknown post validation type: " + post.getPostType());
            }

            String textValidation = contentValidationService.validatePostContent(post.getTitle(), post.getContent());
            String[] text_validation_split = textValidation.split("@");
            if(text_validation_split.length<2){
                throw new RuntimeException("AI returned incorrect form");
            }
            if(text_validation_split[1].equals("AI_UNSAFE")){
                isSafe=false;
            }
            totalComments.append(text_validation_split[0]);
            post.setAiValidationComment(totalComments.toString());

            if(isSafe) post.setFinalAiValidationStatus("AI_SAFE");
            else post.setFinalAiValidationStatus("AI_UNSAFE");
            postRepository.save(post);

        } catch (Exception ermWhatTheSigma) {
            throw new RuntimeException("AI Validation error", ermWhatTheSigma);
        }

    }
}
