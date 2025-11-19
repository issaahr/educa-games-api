package com.educagames.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.educagames.api.model.entity.Lesson;
import com.educagames.api.model.entity.LessonMaterial;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {
    List<LessonMaterial> findByLesson(Lesson lesson);
}
