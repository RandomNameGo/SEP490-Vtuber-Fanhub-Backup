package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.BannerItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerItemRepository extends JpaRepository<BannerItem, Long> {
    Page<BannerItem> findByBannerId(Long bannerId, Pageable pageable);
}