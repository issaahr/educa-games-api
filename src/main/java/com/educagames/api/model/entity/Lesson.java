package com.educagames.api.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.ColumnDefault;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int points;

    @Column(nullable = false, length = 2048)
    private String videoLink;

    @Column(nullable = false)
    private Integer orderIndex;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Builder.Default
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.REMOVE)
    private List<LessonMaterial> materials = new ArrayList<>();
}
