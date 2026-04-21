package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false)
    private Long id;

    @Column(name = "item_name", length = 100)
    private String itemName;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "category", length = 50)
    private String category;

}