package com.educagames.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.announcement.AnnouncementResponseDTO;
import com.educagames.api.model.dto.lesson.ResourceDTO;
import com.educagames.api.model.dto.quiz.QuizDTO;
import com.educagames.api.model.dto.student.CompleteQuizRequestDTO;
import com.educagames.api.model.dto.student.RankingEntryDTO;
import com.educagames.api.model.dto.student.StudentCourseDTO;
import com.educagames.api.model.dto.student.StudentDashboardDTO;
import com.educagames.api.model.dto.student.StudentLessonProgressDTO;
import com.educagames.api.model.dto.student.StudentModuleDTO;
import com.educagames.api.model.dto.student.StudentQuizDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.Course;
import com.educagames.api.model.entity.CourseModule;
import com.educagames.api.model.entity.Lesson;
import com.educagames.api.model.entity.LessonMaterial;
import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.Quiz;
import com.educagames.api.model.entity.QuizAlternative;
import com.educagames.api.model.entity.QuizQuestion;
import com.educagames.api.model.entity.StudentClassroom;
import com.educagames.api.model.entity.StudentLessonProgress;
import com.educagames.api.model.entity.StudentModuleProgress;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.MaterialType;
import com.educagames.api.repository.ClassroomRepository;
import com.educagames.api.repository.CourseModuleRepository;
import com.educagames.api.repository.CourseRepository;
import com.educagames.api.repository.LessonMaterialRepository;
import com.educagames.api.repository.LessonRepository;
import com.educagames.api.repository.ModuleRepository;
import com.educagames.api.repository.QuizAlternativeRepository;
import com.educagames.api.repository.QuizQuestionRepository;
import com.educagames.api.repository.QuizRepository;
import com.educagames.api.repository.StudentClassroomRepository;
import com.educagames.api.repository.StudentLessonProgressRepository;
import com.educagames.api.repository.StudentModuleProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final AuthService authService;
    private final StudentClassroomRepository studentClassroomRepository;
    private final StudentModuleProgressRepository studentModuleProgressRepository;
    private final StudentLessonProgressRepository studentLessonProgressRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAlternativeRepository quizAlternativeRepository;
    private final LessonMaterialRepository lessonMaterialRepository;
    private final ClassroomRepository classroomRepository;
    private final QuizService quizService;
    private final AnnouncementService announcementService;
    private final BadgeService badgeService;

    /**
     * Obtém a turma selecionada do aluno autenticado.
     * Retorna a turma mais recentemente acessada ou a primeira ativa.
     * Se o aluno tiver apenas uma turma ativa e ela não tiver lastAccessAt, atualiza automaticamente.
     *
     * @return Turma selecionada do aluno
     * @throws NotFoundException se o aluno não tiver turma ativa
     */
    @Transactional
    public StudentClassroom getSelectedClassroom() {
        User student = authService.getAuthenticatedUser();
        List<StudentClassroom> activeClassrooms = studentClassroomRepository
            .findActiveByStudentIdOrderedByLastAccess(student.getId());

        if (activeClassrooms.isEmpty()) {
            throw new NotFoundException("Nenhuma turma ativa encontrada para o aluno");
        }

        StudentClassroom selectedClassroom = activeClassrooms.get(0);

        // Se o aluno tem apenas uma turma ativa e ela não tem lastAccessAt, atualiza automaticamente
        if (activeClassrooms.size() == 1 && selectedClassroom.getLastAccessAt() == null) {
            selectedClassroom.setLastAccessAt(LocalDateTime.now());
            studentClassroomRepository.save(selectedClassroom);
        }

        return selectedClassroom;
    }

    /**
     * Obtém os dados do dashboard do aluno.
     *
     * @return DTO com estatísticas do dashboard
     */
    @Transactional(readOnly = true)
    public StudentDashboardDTO getDashboard() {
        User student = authService.getAuthenticatedUser();
        StudentClassroom studentClassroom = getSelectedClassroom();
        Classroom classroom = studentClassroom.getClassroom();

        Integer totalScore = calculateTotalScore(student, classroom);
        Integer rank = calculateRank(student, classroom);
        Integer loginStreak = studentClassroom.getActualLoginStreak();
        Long completedModules = countCompletedModulesForClassroom(student, classroom);
        Long totalModules = countTotalModulesForClassroom(classroom);

        return StudentDashboardDTO.builder()
            .totalScore(totalScore)
            .rank(rank)
            .loginStreak(loginStreak)
            .completedModules(completedModules)
            .totalModules(totalModules)
            .build();
    }

    /**
     * Lista os cursos disponíveis para a turma selecionada do aluno.
     *
     * @return Lista de cursos disponíveis
     */
    @Transactional(readOnly = true)
    public List<StudentCourseDTO> getCourses() {
        StudentClassroom studentClassroom = getSelectedClassroom();
        Classroom classroom = studentClassroom.getClassroom();

        List<Course> courses = classroom.getCourses();
        return courses.stream()
            .map(course -> {
                Long modulesCount = (long) courseModuleRepository.findByCourseId(course.getId()).size();
                return StudentCourseDTO.builder()
                    .id(course.getId())
                    .title(course.getTitle())
                    .description(course.getDescription())
                    .modulesCount(modulesCount)
                    .build();
            })
            .toList();
    }

    /**
     * Lista os módulos de um curso com progresso do aluno.
     *
     * @param courseId ID do curso
     * @return Lista de módulos com progresso
     * @throws NotFoundException se o curso não for encontrado ou não estiver vinculado à turma do aluno
     */
    @Transactional(readOnly = true)
    public List<StudentModuleDTO> getCourseModules(Long courseId) {
        User student = authService.getAuthenticatedUser();
        StudentClassroom studentClassroom = getSelectedClassroom();
        Classroom classroom = studentClassroom.getClassroom();

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Curso não encontrado"));

        if (!classroom.getCourses().contains(course)) {
            throw new NotFoundException("Curso não está vinculado à sua turma");
        }

        List<CourseModule> courseModules = courseModuleRepository.findByCourseId(courseId);
        List<Module> modules = courseModules.stream()
            .sorted(Comparator.comparing(CourseModule::getOrderIndex))
            .map(CourseModule::getModule)
            .toList();

        List<StudentModuleProgress> progressList = studentModuleProgressRepository.findByStudent(student);
        Map<Long, StudentModuleProgress> progressMap = progressList.stream()
            .collect(Collectors.toMap(p -> p.getModule().getId(), p -> p));

        List<StudentLessonProgress> lessonProgressList = studentLessonProgressRepository.findByStudent(student);
        Map<Long, StudentLessonProgress> lessonProgressMap = lessonProgressList.stream()
            .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p));

        List<StudentModuleDTO> result = new ArrayList<>();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            StudentModuleProgress progress = progressMap.get(module.getId());

            boolean isCompleted = progress != null && progress.isCompletedLessons() && progress.isCompletedQuiz();
            boolean isLocked = i > 0 && !isModuleUnlocked(modules.get(i - 1), progressMap.get(modules.get(i - 1).getId()));
            if (i == 0) {
                isLocked = false;
            }

            List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
            int completedLessonsCount = (int) lessons.stream()
                .filter(l -> lessonProgressMap.containsKey(l.getId()) && lessonProgressMap.get(l.getId()).isCompleted())
                .count();
            int progressPercentage = lessons.isEmpty() ? 0 : (completedLessonsCount * 100) / lessons.size();

            result.add(StudentModuleDTO.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(null)
                .lessonsCount(lessons.size())
                .isCompleted(isCompleted)
                .isLocked(isLocked)
                .progress(progressPercentage)
                .lessons(new ArrayList<>())
                .quiz(null)
                .build());
        }

        return result;
    }

    /**
     * Obtém os detalhes completos de um módulo com progresso do aluno.
     *
     * @param moduleId ID do módulo
     * @return DTO com detalhes do módulo e progresso
     * @throws NotFoundException se o módulo não for encontrado ou não estiver acessível ao aluno
     */
    @Transactional(readOnly = true)
    public StudentModuleDTO getModuleDetails(Long moduleId) {
        User student = authService.getAuthenticatedUser();
        StudentClassroom studentClassroom = getSelectedClassroom();
        Classroom classroom = studentClassroom.getClassroom();

        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new NotFoundException("Módulo não encontrado"));

        if (!isModuleAccessible(module, classroom)) {
            throw new NotFoundException("Módulo não está acessível para sua turma");
        }

        StudentModuleProgress moduleProgress = studentModuleProgressRepository
            .findByStudentAndModule(student, module)
            .orElse(null);

        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
        List<StudentLessonProgress> lessonProgressList = studentLessonProgressRepository
            .findByStudentAndLessonModule(student, module);

        Map<Long, StudentLessonProgress> lessonProgressMap = lessonProgressList.stream()
            .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p));

        List<StudentLessonProgressDTO> lessonDTOs = lessons.stream()
            .map(lesson -> {
                StudentLessonProgress progress = lessonProgressMap.get(lesson.getId());
                boolean isCompleted = progress != null && progress.isCompleted();

                List<ResourceDTO> resources = new ArrayList<>();
                if (lesson.getVideoLink() != null && !lesson.getVideoLink().isBlank()) {
                    resources.add(ResourceDTO.builder()
                        .id(null)
                        .type("youtube")
                        .content(lesson.getVideoLink())
                        .label("Vídeo")
                        .build());
                }
                List<LessonMaterial> materials = lessonMaterialRepository.findByLesson(lesson);
                materials.forEach(mat -> resources.add(ResourceDTO.builder()
                    .id(mat.getId())
                    .type(materialTypeToString(mat.getType()))
                    .content(mat.getUrl())
                    .label(mat.getName())
                    .build()));

                return StudentLessonProgressDTO.builder()
                    .id(lesson.getId())
                    .title(lesson.getTitle())
                    .points(lesson.getPoints())
                    .description(lesson.getContent())
                    .isCompleted(isCompleted)
                    .resources(resources)
                    .build();
            })
            .toList();

        QuizDTO quizDTO = quizService.getQuizByModuleForStudent(module);
        boolean allLessonsCompleted = lessons.stream()
            .allMatch(l -> lessonProgressMap.containsKey(l.getId()) && lessonProgressMap.get(l.getId()).isCompleted());
        boolean quizCompleted = moduleProgress != null && moduleProgress.isCompletedQuiz();

        StudentQuizDTO studentQuizDTO = null;
        if (quizDTO != null) {
            studentQuizDTO = StudentQuizDTO.builder()
                .id(quizDTO.getId())
                .isCompleted(quizCompleted)
                .isAvailable(allLessonsCompleted)
                .quiz(quizDTO)
                .build();
        }

        int completedLessonsCount = (int) lessons.stream()
            .filter(l -> lessonProgressMap.containsKey(l.getId()) && lessonProgressMap.get(l.getId()).isCompleted())
            .count();
        int progressPercentage = lessons.isEmpty() ? 0 : (completedLessonsCount * 100) / lessons.size();

        boolean isCompleted = moduleProgress != null && moduleProgress.isCompletedLessons() && moduleProgress.isCompletedQuiz();

        return StudentModuleDTO.builder()
            .id(module.getId())
            .title(module.getTitle())
            .description(null)
            .lessonsCount(lessons.size())
            .isCompleted(isCompleted)
            .isLocked(false)
            .progress(progressPercentage)
            .lessons(lessonDTOs)
            .quiz(studentQuizDTO)
            .build();
    }

    /**
     * Marca uma aula como concluída pelo aluno.
     *
     * @param lessonId ID da aula
     * @throws NotFoundException se a aula não for encontrada ou não estiver acessível
     * @throws BadRequestException se a aula já estiver concluída
     */
    @Transactional
    public void completeLesson(Long lessonId) {
        User student = authService.getAuthenticatedUser();
        StudentClassroom studentClassroom = getSelectedClassroom();
        Classroom classroom = studentClassroom.getClassroom();

        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new NotFoundException("Aula não encontrada"));

        Module module = lesson.getModule();
        if (!isModuleAccessible(module, classroom)) {
            throw new NotFoundException("Aula não está acessível para sua turma");
        }

        Optional<StudentLessonProgress> existingProgress = studentLessonProgressRepository
            .findByStudentAndLesson(student, lesson);

        if (existingProgress.isPresent() && existingProgress.get().isCompleted()) {
            throw new BadRequestException("Aula já está concluída");
        }

        StudentLessonProgress progress = existingProgress.orElseGet(() -> {
            StudentLessonProgress newProgress = new StudentLessonProgress();
            newProgress.setStudent(student);
            newProgress.setLesson(lesson);
            newProgress.setCompleted(false);
            newProgress.setPointsEarned(0);
            return newProgress;
        });

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setPointsEarned(lesson.getPoints());

            updateLoginStreak(studentClassroom);

            studentLessonProgressRepository.save(progress);
            updateModuleProgress(module, student);
        }
    }

    /**
     * Finaliza um quiz e calcula a pontuação do aluno.
     *
     * @param quizId ID do quiz
     * @param request DTO com as respostas do aluno
     * @return Pontuação total obtida no quiz
     * @throws NotFoundException se o quiz não for encontrado ou não estiver acessível
     * @throws BadRequestException se o quiz já estiver concluído ou não estiver disponível
     */
    @Transactional
    public Integer completeQuiz(Long quizId, CompleteQuizRequestDTO request) {
        User student = authService.getAuthenticatedUser();
        StudentClassroom studentClassroom = getSelectedClassroom();
        Classroom classroom = studentClassroom.getClassroom();

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz não encontrado"));

        Module module = quiz.getModule();
        if (!isModuleAccessible(module, classroom)) {
            throw new NotFoundException("Quiz não está acessível para sua turma");
        }

        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
        List<StudentLessonProgress> lessonProgressList = studentLessonProgressRepository
            .findByStudentAndLessonModule(student, module);

        Map<Long, Boolean> completedLessons = lessonProgressList.stream()
            .filter(StudentLessonProgress::isCompleted)
            .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> true));

        boolean allLessonsCompleted = lessons.stream()
            .allMatch(l -> completedLessons.containsKey(l.getId()));

        if (!allLessonsCompleted) {
            throw new BadRequestException("Todas as aulas do módulo devem ser concluídas antes de fazer o quiz");
        }

        StudentModuleProgress moduleProgress = studentModuleProgressRepository
            .findByStudentAndModule(student, module)
            .orElseGet(() -> {
                StudentModuleProgress newProgress = new StudentModuleProgress();
                newProgress.setStudent(student);
                newProgress.setModule(module);
                newProgress.setCompletedLessons(true);
                newProgress.setCompletedQuiz(false);
                newProgress.setPointsEarned(0);
                return newProgress;
            });

        if (moduleProgress.isCompletedQuiz()) {
            throw new BadRequestException("Quiz já foi concluído");
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByQuiz(quiz);
        Map<Long, QuizQuestion> questionMap = questions.stream()
            .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        int totalPoints = 0;
        for (CompleteQuizRequestDTO.QuizAnswerDTO answer : request.getAnswers()) {
            QuizQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }

            List<QuizAlternative> alternatives = quizAlternativeRepository.findByQuestion(question);
            QuizAlternative selectedAlternative = alternatives.stream()
                .filter(a -> a.getId().equals(answer.getSelectedAlternativeId()))
                .findFirst()
                .orElse(null);

            if (selectedAlternative != null && selectedAlternative.isCorrect()) {
                totalPoints += question.getPoints();
            }
        }

        // Calcula os pontos das aulas concluídas do módulo
        List<StudentLessonProgress> completedLessonsProgress = studentLessonProgressRepository
            .findByStudentAndLessonModule(student, module);
        int totalLessonPoints = completedLessonsProgress.stream()
            .filter(StudentLessonProgress::isCompleted)
            .mapToInt(StudentLessonProgress::getPointsEarned)
            .sum();

        moduleProgress.setCompletedQuiz(true);
        moduleProgress.setPointsEarned(totalLessonPoints + totalPoints);
        studentModuleProgressRepository.save(moduleProgress);
        badgeService.checkAndAwardFirstModuleBadge(student, classroom);

        return totalPoints;
    }

    /**
     * Calcula o score de uma tentativa de quiz sem salvar no banco.
     * Compara o ID da alternativa selecionada com o ID da alternativa que tem correct = true.
     * Usado para mostrar o resultado das tentativas antes de finalizar.
     *
     * @param quizId ID do quiz
     * @param request DTO com as respostas (questionId e selectedAlternativeId)
     * @return Pontuação total da tentativa
     * @throws NotFoundException se o quiz não for encontrado
     */
    @Transactional(readOnly = true)
    public Integer calculateQuizAttemptScore(Long quizId, CompleteQuizRequestDTO request) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz não encontrado"));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuiz(quiz);
        Map<Long, QuizQuestion> questionMap = questions.stream()
            .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        int totalPoints = 0;
        for (CompleteQuizRequestDTO.QuizAnswerDTO answer : request.getAnswers()) {
            QuizQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }

            List<QuizAlternative> alternatives = quizAlternativeRepository.findByQuestion(question);
            QuizAlternative selectedAlternative = alternatives.stream()
                .filter(a -> a.getId().equals(answer.getSelectedAlternativeId()))
                .findFirst()
                .orElse(null);

            if (selectedAlternative != null && selectedAlternative.isCorrect()) {
                totalPoints += question.getPoints();
            }
        }

        return totalPoints;
    }

    /**
     * Obtém o ranking da turma selecionada do aluno.
     *
     * @return Lista de alunos ordenados por pontuação
     */
    @Transactional(readOnly = true)
    public List<RankingEntryDTO> getRanking() {
        StudentClassroom studentClassroom = getSelectedClassroom();
        Classroom classroom = studentClassroom.getClassroom();

        List<StudentClassroom> allStudents = studentClassroomRepository
            .findAll()
            .stream()
            .filter(sc -> sc.getClassroom().getId().equals(classroom.getId()) && sc.isActive())
            .toList();

        List<RankingEntryDTO> ranking = allStudents.stream()
            .map(sc -> {
                User student = sc.getStudent();
                Integer score = calculateTotalScore(student, classroom);
                return RankingEntryDTO.builder()
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .score(score)
                    .rank(0)
                    .loginStreak(sc.getActualLoginStreak())
                    .lastAccessAt(sc.getLastAccessAt())
                    .latestBadges(badgeService.getLatestBadgesForStudent(student, classroom, 2))
                    .build();
            })
            .sorted(Comparator.comparing(RankingEntryDTO::getScore).reversed()
                .thenComparing(RankingEntryDTO::getStudentName))
            .collect(Collectors.toList());

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }

        return ranking;
    }

    /**
     * Obtém os avisos da turma selecionada do aluno autenticado.
     * <p>
     * Retorna os avisos direcionados para a turma mais recentemente acessada pelo aluno,
     * ordenados por data de criação (mais recentes primeiro).
     * </p>
     *
     * @return lista de avisos convertidos para DTO
     * @throws NotFoundException se o aluno não tiver turma ativa
     */
    @Transactional(readOnly = true)
    public List<AnnouncementResponseDTO> getAnnouncements() {
        StudentClassroom studentClassroom = getSelectedClassroom();
        Classroom classroom = studentClassroom.getClassroom();
        return announcementService.getAnnouncementsByClassroom(classroom);
    }

    /**
     * Obtém o ranking de uma turma (para instrutor).
     *
     * @param classroomId ID da turma
     * @return Lista de alunos ordenados por pontuação
     * @throws NotFoundException se a turma não for encontrada ou não pertencer ao instrutor
     */
    @Transactional(readOnly = true)
    public List<RankingEntryDTO> getClassroomRanking(Long classroomId) {
        User instructor = authService.getAuthenticatedUser();
        Classroom classroom = classroomRepository.findOneByIdAndInstructorId(classroomId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        List<StudentClassroom> allStudents = studentClassroomRepository
            .findAll()
            .stream()
            .filter(sc -> sc.getClassroom().getId().equals(classroomId) && sc.isActive())
            .toList();

        List<RankingEntryDTO> ranking = allStudents.stream()
            .map(sc -> {
                User student = sc.getStudent();
                Integer score = calculateTotalScore(student, classroom);
                return RankingEntryDTO.builder()
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .score(score)
                    .rank(0)
                    .loginStreak(sc.getActualLoginStreak())
                    .lastAccessAt(sc.getLastAccessAt())
                    .latestBadges(badgeService.getLatestBadgesForStudent(student, classroom, 2))
                    .build();
            })
            .sorted(Comparator.comparing(RankingEntryDTO::getScore).reversed()
                .thenComparing(RankingEntryDTO::getStudentName))
            .collect(Collectors.toList());

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }

        return ranking;
    }

    /**
     * Calcula a pontuação total de um aluno considerando apenas os módulos acessíveis à turma.
     *
     * @param student aluno cuja pontuação será calculada
     * @param classroom turma para filtrar os módulos
     * @return pontuação total do aluno na turma (0 se não houver pontos)
     */
    private Integer calculateTotalScore(User student, Classroom classroom) {
        List<Module> accessibleModules = classroom.getCourses().stream()
            .flatMap(course -> courseModuleRepository.findByCourseId(course.getId()).stream())
            .map(CourseModule::getModule)
            .distinct()
            .toList();

        List<StudentModuleProgress> progressList = studentModuleProgressRepository.findByStudent(student);

        return progressList.stream()
            .filter(progress -> accessibleModules.contains(progress.getModule()))
            .mapToInt(progress -> progress.getPointsEarned() != null ? progress.getPointsEarned() : 0)
            .sum();
    }

    /**
     * Calcula a posição do aluno no ranking da turma.
     * <p>
     * Ordena todos os alunos ativos da turma por pontuação (maior para menor)
     * e retorna a posição do aluno especificado.
     * </p>
     *
     * @param student   aluno cujo ranking será calculado
     * @param classroom turma para a qual calcular o ranking
     * @return posição do aluno no ranking (1 = primeiro lugar)
     */
    private Integer calculateRank(User student, Classroom classroom) {
        List<StudentClassroom> allStudents = studentClassroomRepository
            .findAll()
            .stream()
            .filter(sc -> sc.getClassroom().getId().equals(classroom.getId()) && sc.isActive())
            .toList();

        List<RankingEntryDTO> ranking = allStudents.stream()
            .map(sc -> {
                User s = sc.getStudent();
                Integer score = calculateTotalScore(s, classroom);
                return RankingEntryDTO.builder()
                    .studentId(s.getId())
                    .studentName(s.getName())
                    .score(score)
                    .rank(0)
                    .loginStreak(sc.getActualLoginStreak())
                    .lastAccessAt(sc.getLastAccessAt())
                    .build();
            })
            .sorted(Comparator.comparing(RankingEntryDTO::getScore).reversed()
                .thenComparing(RankingEntryDTO::getStudentName))
            .collect(Collectors.toList());

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }

        return ranking.stream()
            .filter(r -> r.getStudentId().equals(student.getId()))
            .findFirst()
            .map(RankingEntryDTO::getRank)
            .orElse(ranking.size() + 1);
    }

    /**
     * Conta o total de módulos disponíveis para uma turma.
     *
     * @param classroom turma para a qual contar os módulos
     * @return total de módulos disponíveis para a turma
     */
    private Long countTotalModulesForClassroom(Classroom classroom) {
        return classroom.getCourses().stream()
            .mapToLong(course -> courseModuleRepository.findByCourseId(course.getId()).size())
            .sum();
    }

    /**
     * Conta os módulos completados por um aluno considerando apenas os módulos acessíveis à turma.
     *
     * @param student aluno cujos módulos serão contados
     * @param classroom turma para filtrar os módulos
     * @return número de módulos completados na turma
     */
    private Long countCompletedModulesForClassroom(User student, Classroom classroom) {
        List<Module> accessibleModules = classroom.getCourses().stream()
            .flatMap(course -> courseModuleRepository.findByCourseId(course.getId()).stream())
            .map(CourseModule::getModule)
            .distinct()
            .toList();

        List<StudentModuleProgress> progressList = studentModuleProgressRepository.findByStudent(student);

        return progressList.stream()
            .filter(progress -> accessibleModules.contains(progress.getModule()))
            .filter(progress -> progress.isCompletedLessons() && progress.isCompletedQuiz())
            .count();
    }

    /**
     * Verifica se um módulo está acessível para uma turma.
     * <p>
     * Um módulo é acessível se estiver vinculado a algum curso da turma.
     * </p>
     *
     * @param module    módulo a ser verificado
     * @param classroom turma para a qual verificar acesso
     * @return true se o módulo estiver acessível, false caso contrário
     */
    private boolean isModuleAccessible(Module module, Classroom classroom) {
        return classroom.getCourses().stream()
            .flatMap(course -> courseModuleRepository.findByCourseId(course.getId()).stream())
            .anyMatch(cm -> cm.getModule().getId().equals(module.getId()));
    }

    /**
     * Verifica se um módulo está desbloqueado baseado no progresso do módulo anterior.
     * <p>
     * Um módulo está desbloqueado se o módulo anterior foi completamente concluído
     * (todas as aulas e o quiz).
     * </p>
     *
     * @param previousModule   módulo anterior
     * @param previousProgress progresso do módulo anterior
     * @return true se o módulo estiver desbloqueado, false caso contrário
     */
    private boolean isModuleUnlocked(Module previousModule, StudentModuleProgress previousProgress) {
        if (previousProgress == null) {
            return false;
        }
        return previousProgress.isCompletedLessons() && previousProgress.isCompletedQuiz();
    }

    /**
     * Converte um MaterialType enum para sua representação em string.
     *
     * @param t tipo de material a ser convertido
     * @return string representando o tipo (pdf, zip, image, link) ou "link" como padrão
     */
    private String materialTypeToString(MaterialType t) {
        if (t == null) return "link";
        return switch (t) {
            case PDF -> "pdf";
            case ZIP -> "zip";
            case IMAGE -> "image";
            case LINK -> "link";
        };
    }

    /**
     * Atualiza a sequência de login diário do aluno.
     * <p>
     * Incrementa a sequência se o último acesso foi ontem, reinicia se foi antes de ontem,
     * ou mantém se já acessou hoje. Atualiza também o recorde de sequência se necessário.
     * </p>
     *
     * @param studentClassroom vínculo do aluno com a turma
     * @return true se a sequência foi incrementada ou reiniciada, false se já acessou hoje
     */
    private boolean updateLoginStreak(StudentClassroom studentClassroom) {
        LocalDate today = LocalDate.now();
        LocalDate lastStreakDate = studentClassroom.getLastStreakDate();

        if (lastStreakDate == null) {
            studentClassroom.setActualLoginStreak(1);
            studentClassroom.setLongestLoginStreak(1);
            studentClassroom.setLastStreakDate(today);
            studentClassroom.setLastAccessAt(LocalDateTime.now());
            studentClassroomRepository.save(studentClassroom);
            return true;
        }

        if (lastStreakDate.equals(today)) {
            studentClassroom.setLastAccessAt(LocalDateTime.now());
            studentClassroomRepository.save(studentClassroom);
            return false;
        }

        if (lastStreakDate.equals(today.minusDays(1))) {
            int newStreak = studentClassroom.getActualLoginStreak() + 1;
            studentClassroom.setActualLoginStreak(newStreak);
            if (newStreak > studentClassroom.getLongestLoginStreak()) {
                studentClassroom.setLongestLoginStreak(newStreak);
            }
            studentClassroom.setLastStreakDate(today);
            studentClassroom.setLastAccessAt(LocalDateTime.now());
            studentClassroomRepository.save(studentClassroom);
            badgeService.checkAndAwardStreakBadges(
                studentClassroom.getStudent(),
                studentClassroom.getClassroom(),
                newStreak
            );
            return true;
        }

        studentClassroom.setActualLoginStreak(1);
        studentClassroom.setLastStreakDate(today);
        studentClassroom.setLastAccessAt(LocalDateTime.now());
        studentClassroomRepository.save(studentClassroom);
        return true;
    }

    /**
     * Atualiza o progresso de um módulo para um aluno.
     * <p>
     * Verifica se todas as aulas foram concluídas e atualiza o status do módulo.
     * Recalcula os pontos do módulo somando os pontos das aulas concluídas.
     * Se o quiz já foi concluído, mantém os pontos do quiz na soma total.
     * </p>
     *
     * @param module  módulo cujo progresso será atualizado
     * @param student aluno cujo progresso será atualizado
     */
    private void updateModuleProgress(Module module, User student) {
        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
        List<StudentLessonProgress> lessonProgressList = studentLessonProgressRepository
            .findByStudentAndLessonModule(student, module);

        boolean allLessonsCompleted = lessons.stream()
            .allMatch(lesson -> lessonProgressList.stream()
                .anyMatch(p -> p.getLesson().getId().equals(lesson.getId()) && p.isCompleted()));

        // Soma os pontos de todas as aulas concluídas do módulo
        int totalLessonPoints = lessonProgressList.stream()
            .filter(StudentLessonProgress::isCompleted)
            .mapToInt(StudentLessonProgress::getPointsEarned)
            .sum();

        StudentModuleProgress moduleProgress = studentModuleProgressRepository
            .findByStudentAndModule(student, module)
            .orElseGet(() -> {
                StudentModuleProgress newProgress = new StudentModuleProgress();
                newProgress.setStudent(student);
                newProgress.setModule(module);
                newProgress.setCompletedLessons(false);
                newProgress.setCompletedQuiz(false);
                newProgress.setPointsEarned(0);
                return newProgress;
            });

        moduleProgress.setCompletedLessons(allLessonsCompleted);

        if (moduleProgress.isCompletedQuiz()) {
            int currentTotalPoints = moduleProgress.getPointsEarned() != null ? moduleProgress.getPointsEarned() : 0;
            int quizPoints = Math.max(0, currentTotalPoints - totalLessonPoints);
            moduleProgress.setPointsEarned(totalLessonPoints + quizPoints);
        } else {
            moduleProgress.setPointsEarned(totalLessonPoints);
        }

        studentModuleProgressRepository.save(moduleProgress);
    }
}
