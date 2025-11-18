package com.educagames.api.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "modules")
public class Module extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String title;

    // Ownership
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = true)
    private User instructor;

    // Relations
    @OneToOne(mappedBy = "module", cascade = CascadeType.REMOVE)
    private Quiz quiz;

    @Builder.Default
    @OneToMany(mappedBy = "module", cascade = CascadeType.REMOVE)
    private List<CourseModule> courseLinks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "module", cascade = CascadeType.REMOVE)
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons = new ArrayList<>();
}
