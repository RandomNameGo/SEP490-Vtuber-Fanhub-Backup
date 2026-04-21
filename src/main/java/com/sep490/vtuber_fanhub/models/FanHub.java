package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "fan_hub")
public class FanHub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hub_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User ownerUser;

    @Column(name = "subdomain", length = 100)
    private String subdomain;

    @Column(name = "hub_name", length = 100)
    private String hubName;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "banner_url", length = 512)
    private String bannerUrl;

    @Column(name = "background_url", length = 512)
    private String backgroundUrl;

    @Column(name = "theme_color", length = 20)
    private String themeColor;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("0")
    @Column(name = "strike_count")
    private Integer strikeCount;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("0")
    @Column(name = "is_private")
    private Boolean isPrivate;

    @ColumnDefault("0")
    @Column(name = "requires_approval")
    private Boolean requiresApproval;

}