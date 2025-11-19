package com.educagames.api.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.ColumnDefault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
    name = "student_classes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "classroom_id"})
)
public class StudentClassroom extends BaseEntity {

    @Column(nullable = false, length = 10)
    private String enrollment;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int actualLoginStreak;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int longestLoginStreak;

    @Column()
    private LocalDate lastStreakDate;

    @Column()
    private LocalDateTime lastAccessAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Classroom classroom;

}
