package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubReport;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FanHubReportRepository extends JpaRepository<FanHubReport, Long> {

    Page<FanHubReport> findByFanHubId(Long fanHubId, Pageable pageable);

    Page<FanHubReport> findByStatus(String status, Pageable pageable);

    @Query("SELECT fhr FROM FanHubReport fhr WHERE fhr.status = :status")
    List<FanHubReport> findAllByStatus(String status);
}