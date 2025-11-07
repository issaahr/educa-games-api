package com.educagames.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.educagames.api.model.entity.StudentClassroom;

public interface StudentClassroomRepository extends JpaRepository<StudentClassroom, Long> {
    long countByClassroomId(Long classroomId);

    boolean existsByStudentIdAndClassroomId(Long id, Long id1);
}
