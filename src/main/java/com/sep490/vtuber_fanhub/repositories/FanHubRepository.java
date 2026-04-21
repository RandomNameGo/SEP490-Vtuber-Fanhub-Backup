package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FanHubRepository extends JpaRepository<FanHub, Long> {

    Optional<FanHub> findByOwnerUserIdAndIsActive(Long ownerUserId, Boolean isActive);

    Optional<FanHub> findByIdAndIsActive(Long id, Boolean isActive);

    Optional<FanHub> findBySubdomainAndIsActive(String subdomain, Boolean isActive);

    boolean existsBySubdomainAndIsActive(String subdomain, Boolean isActive);

    @Query("SELECT fh FROM FanHub fh WHERE fh.isActive = true AND fh.isPrivate = false ORDER BY fh.createdAt DESC")
    Page<FanHub> findActivePublicFanHubs(Pageable pageable);

    @Query("SELECT fh FROM FanHub fh WHERE fh.isActive = true ORDER BY fh.createdAt DESC")
    Page<FanHub> findAllActiveFanHubs(Pageable pageable);

    @Query("SELECT fh FROM FanHub fh WHERE fh.isActive = true AND fh.isPrivate = false " +
           "ORDER BY (SELECT COUNT(m) FROM FanHubMember m WHERE m.hub = fh AND m.status = 'JOINED') DESC")
    List<FanHub> findTopFanHubsByMemberCount(Pageable pageable);

    @Query("SELECT fh FROM FanHub fh WHERE fh.isActive = true AND fh.isPrivate = false " +
           "AND fh.id IN (SELECT fch.hub.id FROM FanHubCategory fch WHERE fch.categoryName = :categoryName) " +
           "ORDER BY (SELECT COUNT(m) FROM FanHubMember m WHERE m.hub = fh AND m.status = 'JOINED') DESC")
    List<FanHub> findTopFanHubsByMemberCountAndCategory(
            @Param("categoryName") String categoryName,
            Pageable pageable);

    @Query("SELECT fh FROM FanHub fh WHERE fh.isActive = true AND fh.isPrivate = false")
    List<FanHub> findPublicActiveFanHubs(Pageable pageable);

    // Search fan hubs by hub name containing keyword
    @Query("SELECT fh FROM FanHub fh " +
            "WHERE fh.isActive = true " +
            "AND fh.isPrivate = false " +
            "AND LOWER(fh.hubName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FanHub> searchFanHubs(@Param("keyword") String keyword, Pageable pageable);

    long countByIsActiveTrue();

    @Query("SELECT SUM(fh.strikeCount) FROM FanHub fh WHERE fh.isActive = true")
    Long sumTotalStrikes();

    @Query("SELECT COUNT(fh) FROM FanHub fh WHERE fh.isActive = true AND fh.strikeCount > 0")
    long countByIsActiveTrueAndStrikeCountGreaterThanZero();
    }