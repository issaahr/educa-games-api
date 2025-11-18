package com.educagames.api.model.entity;

import jakarta.persistence.*;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "quiz_alternatives")
public class QuizAlternative extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String text;

    @Column(nullable = false)
    private boolean correct;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;
}
