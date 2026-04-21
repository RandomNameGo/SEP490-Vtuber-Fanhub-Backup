package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.VTuberApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VTuberApplicationRepository extends JpaRepository<VTuberApplication, Long> {
    List<VTuberApplication> findByUserId(Long userId);
}