package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {

    List<PostMedia> findByPostId(Long postId);
    List<PostMedia> findByPostIdIn(List<Long> postIds);
    Optional<PostMedia> findBySightEngineMediaId(String sightEngineMediaId);
}
