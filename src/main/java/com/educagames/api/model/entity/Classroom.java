package com.educagames.api.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

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
@Table(name="classrooms")
public class Classroom extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable=false)
    private boolean active;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User instructor;

    @Builder.Default
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.REMOVE)
    private List<StudentClassroom> students = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.REMOVE)
    private List<Invite> invites = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "classrooms")
    private List<Course> courses = new ArrayList<>();

}
