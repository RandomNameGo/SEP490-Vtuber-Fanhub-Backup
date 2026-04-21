package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.FanHubBackground;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FanHubBackgroundRepository extends JpaRepository<FanHubBackground, Long> {

    @Query("select f from FanHubBackground f where f.hub.id = :hubId and f.hub.isActive = true")
    List<FanHubBackground> findByHubId(Long hubId);

    void deleteByHubId(Long hubId);
}
