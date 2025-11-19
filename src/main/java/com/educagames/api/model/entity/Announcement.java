package com.educagames.api.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "announcement_classrooms",
        joinColumns = @JoinColumn(name = "announcement_id"),
        inverseJoinColumns = @JoinColumn(name = "classroom_id")
    )
    private List<Classroom> classrooms = new ArrayList<>();
}
