package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.educagames.api.model.entity.Lesson;
import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.StudentLessonProgress;
import com.educagames.api.model.entity.User;

public interface StudentLessonProgressRepository extends JpaRepository<StudentLessonProgress, Long> {

    Optional<StudentLessonProgress> findByStudentAndLesson(User student, Lesson lesson);

    List<StudentLessonProgress> findByStudent(User student);

    List<StudentLessonProgress> findByLesson(Lesson lesson);

    List<StudentLessonProgress> findByStudentAndLessonModule(User student, Module module);

    @Query("""
        SELECT COALESCE(SUM(slp.pointsEarned), 0) FROM StudentLessonProgress slp
        WHERE slp.student = :student
        """)
    Integer sumPointsEarnedByStudent(@Param("student") User student);
}
