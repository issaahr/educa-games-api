package com.educagames.api.model.entity;

import jakarta.persistence.*;

import com.educagames.api.model.enums.MaterialType;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "lesson_materials")
public class LessonMaterial extends BaseEntity{

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialType type;

    @Column(nullable = false, length = 2048)
    private String url;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
}
