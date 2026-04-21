package com.sep490.vtuber_fanhub.models;

import com.sep490.vtuber_fanhub.models.Enum.MetadataType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "chat_message_metadata")
public class ChatMessageMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @NotNull
    @Lob
    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_type", nullable = false)
    private MetadataType metadataType;

    @Column(name = "target_id")
    private Long targetId;

}