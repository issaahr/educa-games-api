package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsResponseDTO;
import com.educagames.api.model.entity.Classroom ;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("""
        SELECT new com.educagames.api.model.dto.classroom.ClassroomDTO(
            c.id,
            c.name,
            c.active,
            c.createdAt
        )
        FROM Classroom c
        WHERE c.instructor.id = :instructorId
          AND c.active = :active
        ORDER BY c.createdAt DESC
    """)
    List<ClassroomDTO> findActiveClassroomsByInstructorId(
        @Param("instructorId") Long instructorId,
        @Param("active") boolean active
    );

    @Query("""
    SELECT c FROM Classroom c
    WHERE c.active = :active
    AND c.instructor.id = :instructorId
     AND (:searchPattern IS NULL OR
         LOWER(c.name) LIKE :searchPattern)
    """)
    Page<Classroom> findByInstructorIdAndActive(
        @Param("instructorId") Long instructorId,
        @Param("active") boolean active,
        @Param("searchPattern") String searchPattern,
        Pageable pageable
    );

    Optional<Classroom> findOneByIdAndInstructorId(Long id, Long instructorId);

    @Query("""
       SELECT new com.educagames.api.model.dto.classroom.ClassroomDetailsResponseDTO(
           c.id,
           c.name,
           c.createdAt,
           COUNT(DISTINCT sc.id),
           COUNT(DISTINCT cs.id),
           c.active
       )
       FROM Classroom c
       LEFT JOIN c.students sc
       LEFT JOIN c.courses cs
       WHERE c.id = :classroomId
         AND c.instructor.id = :instructorId
       GROUP BY c.id, c.name, c.createdAt, c.active
    """)
    Optional<ClassroomDetailsResponseDTO> findClassroomDetailsByIdAndInstructorId(
        @Param("classroomId") Long classroomId,
        @Param("instructorId") Long instructorId
    );
}
