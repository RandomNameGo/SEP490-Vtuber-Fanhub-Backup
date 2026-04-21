package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
}
