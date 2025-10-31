package com.educagames.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "student_classes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"studentId", "classroomId"})
)
public class StudentClassroom extends BaseEntity {

    @Column(nullable = false)
    private String enrollment;

    @Column(nullable = false)
    private boolean active;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroomId", nullable = false)
    private Classroom classroom;

}
