package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {

    List<VoteOption> findAllByPostId(Long postId);
}