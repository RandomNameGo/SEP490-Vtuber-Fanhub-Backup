package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FanHubMemberRepository extends JpaRepository<FanHubMember, Long> {

    @Query("select f from FanHubMember f where f.hub.id = :fanHubId and f.status = :status and f.hub.isActive = true")
    Page<FanHubMember> findByHubIdAndStatus(Long fanHubId, String status, Pageable pageable);

    @Query("select f from FanHubMember f where f.hub.id = :fanHubId and f.status = 'JOINED' and f.hub.isActive = true")
    Page<FanHubMember> findByHubId(Long fanHubId, Pageable pageable);

    @Query("select f from FanHubMember f where f.hub.id = :fanHubId and f.user.username = :username and f.status = 'JOINED' and f.hub.isActive = true")
    Page<FanHubMember> findByHubIdAndUsername(@Param("fanHubId") Long fanHubId, @Param("username") String username, Pageable pageable);

    @Query("select f from FanHubMember f where f.hub.id = :fanHubId and f.user.id = :userId and f.hub.isActive = true")
    Optional<FanHubMember> findByHubIdAndUserId(Long fanHubId, Long userId);

    Optional<FanHubMember> findByHub_IdAndUser_Id(Long fanHubId, Long userId);

    @Query("select f from FanHubMember f where f.user.id = :userId and f.hub.isActive = true")
    List<FanHubMember> findAllByUserId(Long userId);

    @Query("select count(f) from FanHubMember f where f.user.id = :userId and f.hub.isActive = true")
    long countByUserId(Long userId);

    @Query("select f from FanHubMember f where f.user.id = :userId and f.status = :status and f.hub.isActive = true")
    List<FanHubMember> findByUserIdAndStatus(Long userId, String status);

    @Query("select count(f) from FanHubMember f where f.hub.id = :fanHubId and f.status = 'JOINED' and f.hub.isActive = true")
    long countJoinedMembers(Long fanHubId);

    @Query("select f from FanHubMember f where f.hub.id = :fanHubId and f.status = 'JOINED' and f.hub.isActive = true order by f.fanHubScore desc")
    List<FanHubMember> findTop3ByHubIdOrderByFanHubScoreDesc(Long fanHubId, Pageable pageable);
}