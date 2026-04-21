package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "vtuber_application")
public class VTuberApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "channel_name", nullable = false, length = 50)
    private String channelName;

    @Column(name = "channel_link", nullable = false, length = 512)
    private String channelLink;

    @Lob
    @Column(name = "status")
    private String status;

    @Lob
    @Column(name = "reason")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_by", nullable = false)
    private SystemAccount reviewBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "review_at")
    private Instant reviewAt;

}