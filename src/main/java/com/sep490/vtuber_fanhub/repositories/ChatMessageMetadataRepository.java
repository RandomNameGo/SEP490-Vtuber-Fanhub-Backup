package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.ChatMessageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageMetadataRepository extends JpaRepository<ChatMessageMetadata, Long> {
    Optional<ChatMessageMetadata> findByMessageId(Long chatMessageId);
}