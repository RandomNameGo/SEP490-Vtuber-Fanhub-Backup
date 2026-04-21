package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.BanMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BanMemberRepository extends JpaRepository<BanMember, Long> {

    Optional<BanMember> findByHubIdAndUserIdAndIsActiveTrue(Long hubId, Long userId);

    @Query("SELECT b FROM BanMember b WHERE b.hub.id = :hubId AND b.user.id = :userId AND b.isActive = true AND b.banType IN :banTypes")
    List<BanMember> findByHubIdAndUserIdAndIsActiveTrueAndBanTypeIn(
            @Param("hubId") Long hubId,
            @Param("userId") Long userId,
            @Param("banTypes") List<String> banTypes);

    @Query("SELECT b FROM BanMember b WHERE b.hub.id = :hubId AND b.isActive = true ORDER BY b.createdAt DESC")
    Page<BanMember> findByHubIdAndIsActiveTrue(@Param("hubId") Long hubId, Pageable pageable);

    @Query("SELECT b FROM BanMember b WHERE b.isActive = true AND b.bannedUntil IS NOT NULL AND b.bannedUntil < :now")
    List<BanMember> findExpiredBans(@Param("now") Instant now);

    @Query("SELECT b FROM BanMember b WHERE b.hub.id = :hubId and b.isActive = true")
    Page<BanMember> findByHubId(@Param("hubId") Long hubId, Pageable pageable);
}
