package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hub_id", nullable = false)
    private FanHub hub;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(name = "post_type")
    private String postType;

    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content")
    private String content;

    @ColumnDefault("0")
    @Column(name = "is_pinned")
    private Boolean isPinned;

    @ColumnDefault("'PENDING'")
    @Lob
    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ColumnDefault("'PENDING'")
    @Column(name = "final_ai_validation_status")
    private String finalAiValidationStatus;

    @ColumnDefault("'PENDING'")
    @Column(name = "content_ai_validation_status")
    private String contentAiValidationStatus;

    @Lob
    @Column(name = "ai_validation_comment")
    private String aiValidationComment;

    @Lob
    @Column(name = "ai_validation_last_sent_at")
    private Instant aiValidationLastSentAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "is_announcement")
    private Boolean isAnnouncement;

    @Column(name = "is_schedule")
    private Boolean isSchedule;

}