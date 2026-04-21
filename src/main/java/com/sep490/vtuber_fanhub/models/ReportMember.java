package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "report_member")
public class ReportMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id")
    private FanHub hub;

    @Lob
    @Column(name = "reason")
    private String reason;

    @ColumnDefault("'PENDING'")
    @Lob
    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Lob
    @Column(name = "resolve_message")
    private String resolveMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolve_by")
    private User resolveBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_comment")
    private PostComment relatedComment;

}