package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.UserBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {

    @Query("select ub from UserBookmark ub where ub.user.id = :userId and ub.post.id = :postId")
    Optional<UserBookmark> findByUserIdAndPostId(long userId, long postId);

    @Query("select ub from UserBookmark ub where ub.user.id = :userId")
    Page<UserBookmark> findByUserId(@Param("userId") Long userId, Pageable pageable);
}