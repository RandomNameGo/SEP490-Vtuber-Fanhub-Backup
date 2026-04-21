package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.username = :username and u.isActive is true")
    Optional<User> findByUsernameAndIsActive(String username);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    long countByRole(String role);

    long countByCreatedAtAfter(java.time.Instant timestamp);

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    @Query("SELECT u FROM User u WHERE u.role = 'VTUBER' ORDER BY (SELECT COUNT(follower) FROM User follower WHERE follower.oshiUser = u) DESC")
    List<User> findTopVtubersByOshiCount(org.springframework.data.domain.Pageable pageable);
}