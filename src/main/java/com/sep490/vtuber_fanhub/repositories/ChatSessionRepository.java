package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findByUser_Id(Long id);
}
