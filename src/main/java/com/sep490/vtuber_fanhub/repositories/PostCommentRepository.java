package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId);

    List<PostComment> findByPostOrderByCreatedAtAsc(Post post);

    List<PostComment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);

    Long countByPostId(Long postId);

    List<PostComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    boolean existsByParentCommentId(Long parentCommentId);
}