package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    
    @Query("SELECT b FROM Banner b WHERE b.startTime <= :now AND b.endTime >= :now")
    Optional<Banner> findActiveBanner(@Param("now") Instant now);

    @Query("SELECT b FROM Banner b WHERE b.startTime < :endTime AND b.endTime > :startTime")
    Optional<Banner> findOverlappingBanner(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
}