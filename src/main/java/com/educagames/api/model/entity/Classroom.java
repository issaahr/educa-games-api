package com.educagames.api.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentClassroom> students = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invite> invites = new ArrayList<>();

}
