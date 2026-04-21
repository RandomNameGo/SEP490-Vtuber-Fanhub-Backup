package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Item;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    Page<UserItem> findByUser(User user, Pageable pageable);

    @Query("SELECT ui.item FROM UserItem ui WHERE ui.user = :user AND ui.item IN :items")
    List<Item> findOwnedItemsByUserAndItems(@Param("user") User user, @Param("items") List<Item> items);

    List<UserItem> findByUserAndItem_Category(User user, String category);
}