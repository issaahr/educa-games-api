package com.educagames.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.educagames.api.model.entity.Lesson;
import com.educagames.api.model.entity.Module;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleOrderByOrderIndexAsc(Module module);
}
