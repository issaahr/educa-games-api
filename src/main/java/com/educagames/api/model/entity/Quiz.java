package com.educagames.api.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.ColumnDefault;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "quizzes")
public class Quiz extends BaseEntity{

    @Column(nullable = false)
    @ColumnDefault("0")
    private int points;

    // Relations
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();
}
