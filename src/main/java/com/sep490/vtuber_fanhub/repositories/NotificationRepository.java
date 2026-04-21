package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Notification;
import com.sep490.vtuber_fanhub.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Notification entity
 * Provides database operations for user notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user, ordered by creation date (newest first)
     *
     * @param user the user to find notifications for
     * @param pageable pagination and sorting configuration
     * @return page of notifications
     */
    Page<Notification> findByUser(User user, Pageable pageable);

    /**
     * Find all unread notifications for a specific user
     *
     * @param user the user to find notifications for
     * @param pageable pagination and sorting configuration
     * @return page of unread notifications
     */
    Page<Notification> findByUserAndIsReadFalse(User user, Pageable pageable);

    /**
     * Count unread notifications for a specific user
     *
     * @param user the user to count notifications for
     * @return count of unread notifications
     */
    Long countByUserAndIsReadFalse(User user);

    /**
     * Find notifications by user and notification type
     *
     * @param user the user to find notifications for
     * @param type the notification type (e.g., POST_LIKE, POST_COMMENT)
     * @param pageable pagination and sorting configuration
     * @return page of filtered notifications
     */
    Page<Notification> findByUserAndType(User user, String type, Pageable pageable);

    /**
     * Delete all notifications for a specific user
     * Useful for cleanup or when user deletes account
     *
     * @param user the user to delete notifications for
     */
    void deleteByUser(User user);

    /**
     * Mark all notifications as read for a user
     * Uses JPQL update query for bulk operation
     *
     * @param userId the user ID to mark notifications as read
     * @return number of notifications updated
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * Find the latest notification ID for generating new unique IDs
     *
     * @return the maximum notification ID or null if no notifications exist
     */
    @Query("SELECT MAX(n.id) FROM Notification n")
    Long findMaxId();
}
