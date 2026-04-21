package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "feedback_category")
public class FeedbackCategory {
    @Id
    @Column(name = "feedback_category_id", nullable = false)
    private Long id;

    @Column(name = "category_name", length = 50)
    private String categoryName;

}