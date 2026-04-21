package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop20BySession_Id(Long id);
    @Query("SELECT m FROM ChatMessage m LEFT JOIN FETCH m.metadataList WHERE m.session.id = :sessionId")
    Page<ChatMessage> findAllWithMetadata(@Param("sessionId") Long sessionId, Pageable pageable);
}