package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FeedbackCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackCategoryRepository extends JpaRepository<FeedbackCategory, Long> {
}