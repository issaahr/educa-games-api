package com.educagames.api.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.model.dto.student.BadgeDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.CourseModule;
import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.StudentBadge;
import com.educagames.api.model.entity.StudentClassroom;
import com.educagames.api.model.entity.StudentModuleProgress;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.BadgeType;
import com.educagames.api.repository.CourseModuleRepository;
import com.educagames.api.repository.StudentBadgeRepository;
import com.educagames.api.repository.StudentClassroomRepository;
import com.educagames.api.repository.StudentModuleProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final StudentBadgeRepository studentBadgeRepository;
    private final StudentModuleProgressRepository studentModuleProgressRepository;
    private final StudentClassroomRepository studentClassroomRepository;
    private final CourseModuleRepository courseModuleRepository;

    private static final Map<Integer, BadgeType> STREAK_BADGES = Map.of(
        3, BadgeType.THREE_DAYS_STREAK,
        10, BadgeType.TEN_DAYS_STREAK,
        30, BadgeType.THIRTY_DAYS_STREAK
    );

    /**
     * Verifica e concede badges de streak baseado no valor atual do streak.
     * Só concede se o aluno ainda não tiver a badge específica na turma.
     *
     * @param student aluno que terá as badges verificadas
     * @param classroom turma na qual a badge será concedida
     * @param currentStreak valor atual do streak do aluno
     */
    @Transactional
    public void checkAndAwardStreakBadges(User student, Classroom classroom, int currentStreak) {
        BadgeType badgeType = STREAK_BADGES.get(currentStreak);
        if (badgeType != null) {
            awardBadgeIfNotExists(student, classroom, badgeType);
        }
    }

    /**
     * Verifica e concede a badge de primeiro módulo se o aluno acabou de completar seu primeiro módulo.
     *
     * @param student aluno que terá a badge verificada
     * @param classroom turma na qual a badge será concedida
     */
    @Transactional
    public void checkAndAwardFirstModuleBadge(User student, Classroom classroom) {
        long completedModules = studentModuleProgressRepository
            .countCompletedModulesByStudent(student);

        if (completedModules == 1) {
            awardBadgeIfNotExists(student, classroom, BadgeType.FIRST_MODULE);
        }
    }

    /**
     * Concede uma badge apenas se o aluno ainda não a possuir na turma especificada.
     *
     * @param student aluno que receberá a badge
     * @param classroom turma na qual a badge será concedida
     * @param badgeType tipo da badge a ser concedida
     * @return true se a badge foi concedida, false se o aluno já a possuía
     */
    private boolean awardBadgeIfNotExists(User student, Classroom classroom, BadgeType badgeType) {
        boolean alreadyHasBadge = studentBadgeRepository
            .existsByStudentAndClassroomAndBadgeName(student, classroom, badgeType);

        if (!alreadyHasBadge) {
            StudentBadge badge = StudentBadge.builder()
                .student(student)
                .classroom(classroom)
                .badgeName(badgeType)
                .category(badgeType.getCategory())
                .order(badgeType.getOrder())
                .earnedAt(LocalDateTime.now())
                .build();
            studentBadgeRepository.save(badge);
            return true;
        }
        return false;
    }

    /**
     * Obtém as badges mais recentes de um aluno em uma turma específica.
     * Usa DISTINCT ON para retornar apenas a badge com maior order de cada categoria.
     * Ordena por data de conquista (mais recentes primeiro) e limita o resultado.
     *
     * @param student aluno cujas badges serão buscadas
     * @param classroom turma para filtrar as badges
     * @param limit número máximo de badges a retornar
     * @return lista de badges convertidas para DTO, ordenadas por data de conquista
     */
    public List<BadgeDTO> getLatestBadgesForStudent(User student, Classroom classroom, int limit) {
        List<StudentBadge> latestBadges = studentBadgeRepository
            .findLatestBadgesByCategoryForStudent(student.getId(), classroom.getId());

        return latestBadges.stream()
            .sorted(Comparator.comparing(StudentBadge::getEarnedAt).reversed())
            .limit(limit)
            .map(badge -> BadgeDTO.builder()
                .type(badge.getBadgeName().getCode())
                .earnedAt(badge.getEarnedAt())
                .build())
            .collect(Collectors.toList());
    }


    /**
     * Concede badges retroativamente para todos os alunos com progresso existente.
     * Verifica streak badges baseado no longestLoginStreak e first module badge baseado em módulos completados.
     *
     * @return número de badges concedidas
     */
    @Transactional
    public int awardRetroactiveBadges() {
        List<StudentClassroom> allActiveStudentClassrooms = studentClassroomRepository.findAll()
            .stream()
            .filter(StudentClassroom::isActive)
            .toList();

        int badgesAwarded = 0;

        for (StudentClassroom sc : allActiveStudentClassrooms) {
            User student = sc.getStudent();
            Classroom classroom = sc.getClassroom();

            int longestStreak = sc.getLongestLoginStreak();
            if (longestStreak >= 3 && awardBadgeIfNotExists(student, classroom, BadgeType.THREE_DAYS_STREAK)) {
                badgesAwarded++;
            }
            if (longestStreak >= 10 && awardBadgeIfNotExists(student, classroom, BadgeType.TEN_DAYS_STREAK)) {
                badgesAwarded++;
            }
            if (longestStreak >= 30 && awardBadgeIfNotExists(student, classroom, BadgeType.THIRTY_DAYS_STREAK)) {
                badgesAwarded++;
            }

            long completedModules = countCompletedModulesForClassroom(student, classroom);
            if (completedModules >= 1 && awardBadgeIfNotExists(student, classroom, BadgeType.FIRST_MODULE)) {
                badgesAwarded++;
            }
        }

        return badgesAwarded;
    }

    /**
     * Conta o número de módulos completados por um aluno em uma turma específica.
     *
     * @param student aluno cujos módulos serão contados
     * @param classroom turma para filtrar os módulos
     * @return número de módulos completados na turma
     */
    private long countCompletedModulesForClassroom(User student, Classroom classroom) {
        List<Module> accessibleModules = classroom.getCourses().stream()
            .flatMap(course -> courseModuleRepository.findByCourseId(course.getId()).stream())
            .map(CourseModule::getModule)
            .distinct()
            .collect(Collectors.toList());

        List<StudentModuleProgress> progressList = studentModuleProgressRepository.findByStudent(student);
        return progressList.stream()
            .filter(p -> accessibleModules.contains(p.getModule()))
            .filter(p -> p.isCompletedLessons() && p.isCompletedQuiz())
            .count();
    }
}

