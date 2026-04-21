package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "banner")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 512)
    private String name;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "gacha_cost")
    private Integer gachaCost;

    @Column(name = "created_at")
    private Instant createdAt;

    @Lob
    @Column(name = "banner_img_url")
    private String bannerImgUrl;

    @Column(name = "is_active")
    private Boolean isActive;

}