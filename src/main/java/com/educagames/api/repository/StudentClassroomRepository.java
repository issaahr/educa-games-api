package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.educagames.api.model.dto.classroom.StudentClassroomResponseDTO;
import com.educagames.api.model.entity.StudentClassroom;

public interface StudentClassroomRepository extends JpaRepository<StudentClassroom, Long> {
    long countByClassroomId(Long classroomId);

    boolean existsByStudentIdAndClassroomId(Long id, Long id1);

    Optional<StudentClassroom> findByIdAndClassroom_Id(Long id, Long classroomId);

    @Query("""
    SELECT new com.educagames.api.model.dto.classroom.StudentClassroomResponseDTO(
        sc.id,
        sc.createdAt,
        s.name,
        s.email,
        sc.enrollment,
        sc.active
    )
    FROM StudentClassroom sc
    INNER JOIN sc.student s
    WHERE sc.classroom.id = :classroomId
      AND sc.active = :active
      AND (:searchPattern IS NULL OR
           LOWER(s.name) LIKE :searchPattern
           OR LOWER(s.email) LIKE :searchPattern)
    """)
    Page<StudentClassroomResponseDTO> findStudentsByClassroomIdAndActive(
        @Param("classroomId") Long classroomId,
        @Param("active") boolean active,
        @Param("searchPattern") String searchPattern,
        Pageable pageable
    );

    @Query("""
        SELECT sc.classroom.id, sc.classroom.name FROM StudentClassroom sc
        WHERE sc.student.id = :studentId
        AND sc.active = true
        AND sc.classroom.active = true
        """)
    List<Object[]> findActiveClassroomIdAndNameByStudentId(@Param("studentId") Long studentId);

    Optional<StudentClassroom> findByStudentIdAndClassroomIdAndActive(Long studentId, Long classroomId, boolean active);

    @Query("""
        SELECT sc FROM StudentClassroom sc
        WHERE sc.student.id = :studentId
        AND sc.classroom.id = :classroomId
        AND sc.active = true
        AND sc.classroom.active = true
        """)
    Optional<StudentClassroom> findActiveByStudentIdAndClassroomId(
        @Param("studentId") Long studentId,
        @Param("classroomId") Long classroomId
    );

    @Query("""
        SELECT sc FROM StudentClassroom sc
        WHERE sc.student.id = :studentId
        AND sc.active = true
        AND sc.classroom.active = true
        ORDER BY sc.lastAccessAt DESC NULLS LAST, sc.createdAt DESC
        """)
    List<StudentClassroom> findActiveByStudentIdOrderedByLastAccess(@Param("studentId") Long studentId);

    @Query("""
        SELECT sc FROM StudentClassroom sc
        WHERE sc.student.id = :studentId
        AND sc.classroom.id = :classroomId
        """)
    Optional<StudentClassroom> findByStudentIdAndClassroomId(
        @Param("studentId") Long studentId,
        @Param("classroomId") Long classroomId
    );
}
