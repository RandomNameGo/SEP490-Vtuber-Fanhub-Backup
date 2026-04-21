package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {

    List<PostHashtag> findByPostId(Long postId);
}