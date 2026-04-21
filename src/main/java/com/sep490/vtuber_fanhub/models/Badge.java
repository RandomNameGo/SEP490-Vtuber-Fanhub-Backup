package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "badge")
public class Badge {
    @Id
    @Column(name = "badge_id", nullable = false)
    private Long id;

    @Column(name = "badge_name", length = 100)
    private String badgeName;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;

    @Lob
    @Column(name = "requirement")
    private String requirement;

}