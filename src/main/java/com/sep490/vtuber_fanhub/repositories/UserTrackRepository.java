package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.UserTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTrackRepository extends JpaRepository<UserTrack, Long> {
    Optional<UserTrack> findByUserId(Long userId);
}