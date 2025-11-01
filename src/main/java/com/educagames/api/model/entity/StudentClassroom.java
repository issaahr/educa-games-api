package com.educagames.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "student_classes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "classroom_id"})
)
public class StudentClassroom extends BaseEntity {

    @Column(nullable = false)
    private String enrollment;

    @Column(nullable = false)
    private boolean active;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Classroom classroom;

}
