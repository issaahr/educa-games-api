package com.educagames.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByModule(Module module);
}
