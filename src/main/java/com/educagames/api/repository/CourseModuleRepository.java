package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.educagames.api.model.entity.CourseModule;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {

    List<CourseModule> findByCourseId(Long courseId);

    List<CourseModule> findByModuleId(Long moduleId);

    @Query("""
        SELECT cm FROM CourseModule cm
        WHERE cm.course.instructor.id = :instructorId
          AND cm.module.id = :moduleId
    """)
    Optional<CourseModule> findOneByModuleIdAndInstructor(Long moduleId, Long instructorId);
}
