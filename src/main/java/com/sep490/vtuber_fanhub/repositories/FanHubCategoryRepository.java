package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FanHubCategoryRepository extends JpaRepository<FanHubCategory, Long> {

    @Query("select f from FanHubCategory f where f.hub.id = :hubId and f.hub.isActive = true")
    List<FanHubCategory> findByHubId(Long hubId);

    void deleteByHubId(Long hubId);
}