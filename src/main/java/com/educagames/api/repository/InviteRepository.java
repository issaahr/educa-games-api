package com.educagames.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.educagames.api.model.entity.Invite;
import com.educagames.api.model.enums.Role;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    Optional<Invite> findByEmail(String email);

    Optional<Invite> findByToken(String token);

    @Query("""
    SELECT i FROM Invite i
    WHERE i.role = :role
    AND i.classroom IS NULL
    AND i.status != 'ACCEPTED'
    AND (:searchPattern IS NULL OR LOWER(i.email) LIKE :searchPattern)
    """)
    Page<Invite> findInstructorInvites(
        @Param("role") Role role,
        @Param("searchPattern") String searchPattern,
        Pageable pageable
    );

    @Query("""
    SELECT i FROM Invite i
    WHERE i.role = :role
    AND i.classroom.id = :classroomId
    AND i.classroom.instructor.id = :instructorId
    AND i.status != 'ACCEPTED'
    AND (:searchPattern IS NULL OR LOWER(i.email) LIKE :searchPattern)
    """)
    Page<Invite> findStudentInvites(
        @Param("role") Role role,
        @Param("classroomId") Long classroomId,
        @Param("instructorId") Long instructorId,
        @Param("searchPattern") String searchPattern,
        Pageable pageable
    );

    Optional<Invite> findByClassroomIdAndEmail(Long classroomId, String email);

    boolean existsByIdAndSenderId(Long id, Long senderId);
}
