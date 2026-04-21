package com.sep490.vtuber_fanhub.events;

import com.sep490.vtuber_fanhub.models.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// I need to fire the post validation post like this, my older approach to it no longer work
// the reason is that I need the database to commit the post completely before firing the post validation job.
// I simply could've used .flush() but I didn't, I'm that guy.
@Getter
@RequiredArgsConstructor
public class PostCreatedEvent {
    private final Post post;
}
