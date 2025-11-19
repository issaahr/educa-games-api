package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.educagames.api.model.entity.Announcement;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.User;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByInstructorOrderByCreatedAtDesc(User instructor);

    Optional<Announcement> findByIdAndInstructor(Long id, User instructor);

    @Query("""
        SELECT DISTINCT a FROM Announcement a
        JOIN a.classrooms c
        WHERE c = :classroom
        ORDER BY a.createdAt DESC
        """)
    List<Announcement> findByClassroomOrderByCreatedAtDesc(@Param("classroom") Classroom classroom);
}
