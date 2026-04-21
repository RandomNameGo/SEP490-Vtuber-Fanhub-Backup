package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "fan_hub_members")
public class FanHubMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hub_id", nullable = false)
    private FanHub hub;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(name = "role_in_hub")
    private String roleInHub;

    @ColumnDefault("'JOINED'")
    @Lob
    @Column(name = "status")
    private String status;

    @ColumnDefault("0")
    @Column(name = "fan_hub_score")
    private Integer fanHubScore;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Lob
    @Column(name = "title")
    private String title;

}