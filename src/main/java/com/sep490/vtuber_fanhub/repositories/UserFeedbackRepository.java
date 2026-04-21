package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.UserFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {

    Page<UserFeedback> findByUserId(Long userId, Pageable pageable);

    Optional<UserFeedback> findByIdAndUserId(Long id, Long userId);

}