package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "fan_hub_model")
public class FanHubModel {
    @Id
    @Column(name = "model_id", nullable = false)
    private Long id;

    @Column(name = "name", length = 512)
    private String name;

    @Lob
    @Column(name = "file_url")
    private String fileUrl;

    @Lob
    @Column(name = "sprite_url")
    private String spriteUrl;

    @Column(name = "created_at")
    private Instant createdAt;

}