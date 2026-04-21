package com.sep490.vtuber_fanhub.listeners;

import com.sep490.vtuber_fanhub.events.PostCreatedEvent;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.services.PostValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;


@Component
@RequiredArgsConstructor
public class PostValidationListener {

    private final PostValidationService postValidationServiceImplAsync;
    private final PostRepository postRepository;

    // after committing the post completely do we fire the ai validation job
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("validationExecutor")
    public void handlePostCreated(PostCreatedEvent event) {
        try{
            Post post = event.getPost();
            post.setAiValidationLastSentAt(Instant.now());
            postRepository.save(post);
            postValidationServiceImplAsync.validatePost(event.getPost());
        }
        catch(Exception e){
            throw new RuntimeException("Error while firing post validation job");
        }
    }
}
