package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.educagames.api.model.entity.Module;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    @Query("""
        SELECT DISTINCT m FROM Module m
        LEFT JOIN m.courseLinks cm
        WHERE m.instructor.id = :instructorId
          AND (:courseId IS NULL OR cm.course.id = :courseId)
        ORDER BY m.createdAt DESC
    """)
    List<Module> findByInstructorAndOptionalCourse(Long instructorId, Long courseId);

    @Query("""
        SELECT m FROM Module m
        WHERE m.instructor.id = :instructorId
          AND m.id = :id
    """)
    Optional<Module> findOneByIdAndInstructor(Long id, Long instructorId);

    @Query("""
        SELECT DISTINCT m FROM Module m
        LEFT JOIN m.courseLinks cm
        WHERE m.instructor.id = :instructorId
          AND (:courseId IS NULL OR cm.course.id = :courseId)
          AND (:searchPattern IS NULL OR LOWER(m.title) LIKE :searchPattern)
        ORDER BY m.createdAt DESC
    """)
    Page<Module> findByInstructorAndOptionalCoursePaged(Long instructorId, Long courseId, String searchPattern, Pageable pageable);
}
