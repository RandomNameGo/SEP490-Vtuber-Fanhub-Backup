package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.ReportMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportMemberRepository extends JpaRepository<ReportMember, Long> {

    @Query("SELECT rm FROM ReportMember rm WHERE rm.hub.id = :fanHubId")
    Page<ReportMember> findByFanHubId(@Param("fanHubId") Long fanHubId, Pageable pageable);

    Page<ReportMember> findByReportedById(Long reportedById, Pageable pageable);

    Page<ReportMember> findByHubIdAndStatus(Long fanHubId, String status, Pageable pageable);

    List<ReportMember> findByUserId(Long userId);
}
