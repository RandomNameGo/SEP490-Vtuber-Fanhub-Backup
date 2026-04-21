package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    @Query("SELECT pv FROM PostVote pv WHERE pv.user.id = :userId AND pv.option.id = :optionId")
    Optional<PostVote> findByUserIdAndOptionId(@Param("userId") Long userId, @Param("optionId") Long optionId);

    @Query("SELECT pv FROM PostVote pv WHERE pv.user.id = :userId AND pv.option.post.id = :postId")
    List<PostVote> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("SELECT COUNT(pv) FROM PostVote pv WHERE pv.option.id = :optionId")
    Long countByOptionId(@Param("optionId") Long optionId);

    @Query("SELECT COUNT(pv) FROM PostVote pv WHERE pv.option.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);
}