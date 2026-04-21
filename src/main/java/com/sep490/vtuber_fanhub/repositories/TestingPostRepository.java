package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestingPostRepository extends JpaRepository<Post, Long> {
    Post findTop1ByOrderByIdDesc();
}
