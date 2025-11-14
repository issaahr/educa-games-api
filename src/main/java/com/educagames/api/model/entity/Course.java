package com.educagames.api.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="courses")
public class Course extends BaseEntity{
    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "classroom_courses",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "classroom_id")
    )
    private List<Classroom> classrooms = new ArrayList<>();

}
