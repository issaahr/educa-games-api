package com.educagames.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.StudentBadge;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.BadgeType;

@Repository
public interface StudentBadgeRepository extends JpaRepository<StudentBadge, Long> {

    boolean existsByStudentAndClassroomAndBadgeName(User student, Classroom classroom, BadgeType badgeName);

    @Query(value = """
        SELECT DISTINCT ON (category) *
        FROM student_badges
        WHERE student_id = :studentId AND classroom_id = :classroomId
        ORDER BY category, badge_order DESC, earned_at DESC
        """, nativeQuery = true)
    List<StudentBadge> findLatestBadgesByCategoryForStudent(@Param("studentId") Long studentId, @Param("classroomId") Long classroomId);

    @Query("""
        SELECT sb FROM StudentBadge sb
        WHERE sb.student = :student AND sb.classroom = :classroom
        ORDER BY sb.earnedAt DESC
        """)
    List<StudentBadge> findByStudentAndClassroomOrderByEarnedAtDesc(@Param("student") User student, @Param("classroom") Classroom classroom);
}

