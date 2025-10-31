package com.educagames.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "instructorId", nullable = false)
    private User instructor;

    @Builder.Default
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentClassroom> students = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private  List<Invite> invites = new ArrayList<>();

}
