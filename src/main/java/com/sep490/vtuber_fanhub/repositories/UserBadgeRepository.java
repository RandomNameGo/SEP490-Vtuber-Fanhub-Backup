package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId")
    List<UserBadge> findByUserId(@Param("userId") Long userId);

    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId AND ub.isDisplay = true")
    List<UserBadge> findByUserIdAndIsDisplayTrue(@Param("userId") Long userId);

    long countByUserId(@Param("userId") Long userId);
}
