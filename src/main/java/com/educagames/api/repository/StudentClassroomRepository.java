package com.educagames.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.educagames.api.model.entity.StudentClassroom;

public interface StudentClassroomRepository extends JpaRepository<StudentClassroom, Long> {
    long countByClassroomId(Long classroomId);

    boolean existsByStudentIdAndClassroomId(Long id, Long id1);

    List<StudentClassroom> findByStudentId(Long studentId);

    List<StudentClassroom> findByStudentIdAndActiveTrue(Long studentId);

    @Query("""
        SELECT sc.classroom.id, sc.classroom.name FROM StudentClassroom sc
        WHERE sc.student.id = :studentId
        AND sc.active = true
        AND sc.classroom.active = true
        """)
    List<Object[]> findActiveClassroomIdAndNameByStudentId(@Param("studentId") Long studentId);
}
