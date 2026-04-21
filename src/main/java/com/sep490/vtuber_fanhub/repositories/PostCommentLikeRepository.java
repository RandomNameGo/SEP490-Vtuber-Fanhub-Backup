package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PostComment;
import com.sep490.vtuber_fanhub.models.PostCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostCommentLikeRepository extends JpaRepository<PostCommentLike, Long> {

    Long countByComment(PostComment comment);

    Optional<PostCommentLike> findByUserIdAndComment(Long userId, PostComment comment);
}
