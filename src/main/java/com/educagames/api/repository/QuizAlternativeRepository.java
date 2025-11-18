package com.educagames.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.educagames.api.model.entity.QuizAlternative;
import com.educagames.api.model.entity.QuizQuestion;

public interface QuizAlternativeRepository extends JpaRepository<QuizAlternative, Long> {
    List<QuizAlternative> findByQuestion(QuizQuestion question);
}
