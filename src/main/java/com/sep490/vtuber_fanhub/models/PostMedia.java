package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.UniqueElements;

@Getter
@Setter
@Entity
@Table(name = "post_medias")
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_media_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Size(max = 512)
    @Column(name = "media_url", length = 512)
    private String mediaUrl;

    @ColumnDefault("'Pending'")
    @Lob
    @Column(name = "ai_validation_status")
    private String aiValidationStatus;

    @Size(max = 500)
    @Column(name = "ai_validation_comment", length = 500)
    private String aiValidationComment;

    @Size(max = 45)
    @Column(name = "sight_engine_media_id", length = 45, unique = true, nullable = true)
    private String sightEngineMediaId;

}