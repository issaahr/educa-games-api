package com.educagames.api.model.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.ColumnDefault;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
    name = "student_lesson_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "lesson_id"})
)
public class StudentLessonProgress extends BaseEntity {

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer pointsEarned;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
}
