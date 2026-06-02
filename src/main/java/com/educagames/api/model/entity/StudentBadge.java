package com.educagames.api.model.entity;

import java.time.LocalDateTime;

import com.educagames.api.model.enums.BadgeCategory;
import com.educagames.api.model.enums.BadgeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
    name = "student_badges",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "classroom_id", "badge_name"})
)
public class StudentBadge extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_name", nullable = false)
    private BadgeType badgeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private BadgeCategory category;

    @Column(name = "badge_order", nullable = false)
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;
}

