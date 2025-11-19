package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.educagames.api.model.entity.Course;
import com.educagames.api.repository.projection.CourseSummary;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findOneByIdAndInstructorId(Long id, Long instructorId);

    List<Course> findByIdInAndInstructorId(List<Long> ids, Long instructorId);

    List<CourseSummary> findByInstructorId(Long instructorId);

    @Query("""
        SELECT
            c.id AS id,
            c.title AS title,
            c.description AS description
        FROM Course c
        JOIN c.classrooms cl
        WHERE c.instructor.id = :instructorId
            AND cl.id = :classroomId
            AND (:searchPattern IS NULL
                 OR LOWER(c.title) LIKE :searchPattern)
        ORDER BY c.createdAt DESC
    """)
    Page<CourseSummary> findByInstructorIdAndClassroomId(Long instructorId, Long classroomId, String searchPattern, Pageable pageable);
}
