package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.UserDailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission, Long> {

    Optional<UserDailyMission> findByUserId(Long userId);

}