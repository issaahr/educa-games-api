package com.educagames.api.model.entity;

import jakarta.persistence.*;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
    name = "course_modules",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "module_id"})
)
public class CourseModule extends BaseEntity {

    @Column(nullable = false)
    private Integer orderIndex;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
}
