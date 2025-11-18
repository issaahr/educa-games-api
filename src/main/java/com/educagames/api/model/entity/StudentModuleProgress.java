package com.educagames.api.model.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.ColumnDefault;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
    name = "student_module_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "module_id"})
)
public class StudentModuleProgress extends BaseEntity {

    @Column(nullable = false)
    private boolean completedLessons;

    @Column(nullable = false)
    private boolean completedQuiz;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer pointsEarned;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
}
