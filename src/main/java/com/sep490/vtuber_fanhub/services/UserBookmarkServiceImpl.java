package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserBookmark;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.UserBookmarkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserBookmarkServiceImpl implements UserBookmarkService {

    private final UserBookmarkRepository userBookmarkRepository;

    private final HttpServletRequest httpServletRequest;

    private final AuthService authService;

    private final PostRepository postRepository;

    @Override
    public String createUserBookmark(long postId) {

        User currentUser = authService.getUserFromToken(httpServletRequest);

        Optional<Post> post = postRepository.findById(postId);

        if(post.isEmpty()){
            throw new NotFoundException("Post not found");
        }

        Optional<UserBookmark> savedUserBookmark = userBookmarkRepository.findByUserIdAndPostId(currentUser.getId(), postId);

        savedUserBookmark.ifPresent(userBookmarkRepository::delete);

        UserBookmark userBookmark = new UserBookmark();
        userBookmark.setPost(post.get());
        userBookmark.setUser(currentUser);
        userBookmark.setCreatedAt(Instant.now());
        userBookmarkRepository.save(userBookmark);

        return "";
    }
}
