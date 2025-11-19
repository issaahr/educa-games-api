package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.StudentModuleProgress;
import com.educagames.api.model.entity.User;

public interface StudentModuleProgressRepository extends JpaRepository<StudentModuleProgress, Long> {

    Optional<StudentModuleProgress> findByStudentAndModule(User student, Module module);

    List<StudentModuleProgress> findByStudent(User student);

    @Query("""
        SELECT COUNT(smp) FROM StudentModuleProgress smp
        WHERE smp.student = :student
        AND smp.completedLessons = true
        AND smp.completedQuiz = true
        """)
    long countCompletedModulesByStudent(@Param("student") User student);

    @Query("""
        SELECT COALESCE(SUM(smp.pointsEarned), 0) FROM StudentModuleProgress smp
        WHERE smp.student = :student
        """)
    Integer sumPointsEarnedByStudent(@Param("student") User student);
}
