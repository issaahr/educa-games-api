package com.educagames.api.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsResponseDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.classroom.EditClassRequestDTO;
import com.educagames.api.model.dto.classroom.StudentClassroomResponseDTO;
import com.educagames.api.model.dto.classroom.StudentProfileDTO;
import com.educagames.api.model.dto.classroom.StudentReportDTO;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.OnlyIdsDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.student.RankingEntryDTO;
import com.educagames.api.model.dto.user.ChangeUserStatusDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.Course;
import com.educagames.api.model.entity.CourseModule;
import com.educagames.api.model.entity.Lesson;
import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.StudentClassroom;
import com.educagames.api.model.entity.StudentLessonProgress;
import com.educagames.api.model.entity.StudentModuleProgress;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.ClassroomRepository;
import com.educagames.api.repository.CourseModuleRepository;
import com.educagames.api.repository.CourseRepository;
import com.educagames.api.repository.LessonRepository;
import com.educagames.api.repository.StudentClassroomRepository;
import com.educagames.api.repository.StudentLessonProgressRepository;
import com.educagames.api.repository.StudentModuleProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentClassroomRepository studentClassroomRepository;
    private final AuthService authService;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final LessonRepository lessonRepository;
    private final StudentLessonProgressRepository studentLessonProgressRepository;
    private final StudentModuleProgressRepository studentModuleProgressRepository;

    /**
     * Cria uma nova turma associada ao instrutor autenticado.
     * <p>
     * A turma é criada com status ativo e vinculada ao instrutor logado.
     * </p>
     *
     * @param dto DTO contendo o nome da turma a ser criada
     */
    public void createClass(CreateClassRequestDTO dto) {
        User instructor = authService.getAuthenticatedUser();

        Classroom classroom = new Classroom();
        classroom.setName(dto.getName());
        classroom.setActive(true);
        classroom.setInstructor(instructor);
        classroomRepository.save(classroom);
    }

    /**
     * Lista todas as turmas do instrutor autenticado.
     * <p>
     * Retorna apenas as turmas pertencentes ao instrutor logado.
     * </p>
     *
     * @return lista de turmas convertidas para DTO
     */
    public PageResponseDTO<ClassroomDTO> listClasses (boolean active, String search, Pageable pageable){
        User instructor = authService.getAuthenticatedUser();

        String searchPattern = search != null && !search.trim().isEmpty()
            ? "%" + search.toLowerCase() + "%"
            : null;

        Page<Classroom> classes = classroomRepository.findByInstructorIdAndActive(
            instructor.getId(), active, searchPattern, pageable
        );

        Page<ClassroomDTO> classroomDTOS = classes.map(classroom -> {
            ClassroomDTO dto = new ClassroomDTO();
            dto.setId(classroom.getId());
            dto.setName(classroom.getName());
            dto.setActive(classroom.isActive());
            dto.setCreatedAt(classroom.getCreatedAt());
            return dto;
        });

        return PageResponseDTO.<ClassroomDTO>builder()
            .content(classroomDTOS.getContent())
            .totalElements(classroomDTOS.getTotalElements())
            .totalPages(classroomDTOS.getTotalPages())
            .size(classroomDTOS.getSize())
            .number(classroomDTOS.getNumber())
            .first(classroomDTOS.isFirst())
            .last(classroomDTOS.isLast())
            .build();
    }

    /**
     * Obtém os detalhes completos de uma turma do instrutor autenticado.
     * <p>
     * Garante que a turma pertence ao instrutor logado antes de retornar
     * as informações detalhadas.
     * </p>
     *
     * @param id ID da turma a ser consultada
     * @return detalhes da turma (nome, status e informações agregadas)
     * @throws NotFoundException caso a turma não exista ou não pertença ao instrutor
     */
    public ClassroomDetailsResponseDTO getClassById(Long id) {
        User instructor = authService.getAuthenticatedUser();

        return classroomRepository
            .findClassroomDetailsByIdAndInstructorId(id, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));
    }

    /**
     * Lista estudantes de uma turma com paginação, filtro por status e busca.
     * <p>
     * Valida que a turma pertence ao instrutor autenticado. O parâmetro
     * {@code active} indica se devem ser listados apenas alunos ativos ou inativos.
     * A busca filtra por nome/email do aluno.
     * </p>
     *
     * @param classroomId ID da turma
     * @param active      filtra por alunos ativos (true) ou inativos (false)
     * @param search      termo de busca (opcional)
     * @param pageable    configuração de paginação e ordenação
     * @return página de alunos da turma
     * @throws NotFoundException caso a turma não exista ou não pertença ao instrutor
     */
    public PageResponseDTO<StudentClassroomResponseDTO> listStudentsByClass(
        Long classroomId, boolean active, String search, Pageable pageable
    ) {
        User instructor = authService.getAuthenticatedUser();

        classroomRepository.findOneByIdAndInstructorId(classroomId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        String searchPattern = search != null && !search.trim().isEmpty()
            ? "%" + search.toLowerCase() + "%"
            : null;

        Page<StudentClassroomResponseDTO> students = studentClassroomRepository
            .findStudentsByClassroomIdAndActive(classroomId, active, searchPattern, pageable);

        return PageResponseDTO.<StudentClassroomResponseDTO>builder()
            .content(students.getContent())
            .totalElements(students.getTotalElements())
            .totalPages(students.getTotalPages())
            .size(students.getSize())
            .number(students.getNumber())
            .first(students.isFirst())
            .last(students.isLast())
            .build();
    }

    /**
     * Edita os dados de uma turma do instrutor autenticado.
     * <p>
     * Atualiza nome e/ou status ativo conforme o DTO fornecido. Não realiza
     * atualização se o valor informado for igual ao atual.
     * </p>
     *
     * @param id  ID da turma
     * @param dto dados de edição (nome e ativo)
     * @throws NotFoundException caso a turma não exista ou não pertença ao instrutor
     */
    public void editClass(Long id, EditClassRequestDTO dto) {
        User instructor = authService.getAuthenticatedUser();

        Classroom classroom = classroomRepository.findOneByIdAndInstructorId(id, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()
            && !dto.getName().trim().equalsIgnoreCase(classroom.getName())) {
            classroom.setName(dto.getName().trim());
        }

        if (dto.getActive() != null && dto.getActive() != classroom.isActive()) {
            classroom.setActive(dto.getActive());
        }

        classroomRepository.save(classroom);

    }

    /**
     * Exclui uma turma do instrutor autenticado.
     *
     * @param id ID da turma a ser excluída
     * @throws NotFoundException caso a turma não exista ou não pertença ao instrutor
     */
    public void deleteClass(Long id) {
        User instructor = authService.getAuthenticatedUser();

        Classroom classroom = classroomRepository.findOneByIdAndInstructorId(id, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        classroomRepository.delete(classroom);
    }

    /**
     * Atualiza o status (ativo/inativo) de um estudante em uma turma.
     * <p>
     * Valida que a turma pertence ao instrutor autenticado e que o estudante
     * está vinculado à turma antes de atualizar o status.
     * </p>
     *
     * @param dto         DTO com o ID do vínculo e novo status
     * @param classroomId ID da turma
     * @throws NotFoundException caso turma ou estudante não sejam encontrados
     */
    public void updateStudentStatus(ChangeUserStatusDTO dto, Long classroomId){
        User instructor = authService.getAuthenticatedUser();

        classroomRepository.findOneByIdAndInstructorId(classroomId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        StudentClassroom studentClassroom = studentClassroomRepository.findByIdAndClassroom_Id(dto.getId(), classroomId)
            .orElseThrow(() -> new NotFoundException("Estudante não encontrado na turma"));

        studentClassroom.setActive(dto.isStatus());
        studentClassroomRepository.save(studentClassroom);

    }

    /**
     * Remove o vínculo de um estudante com uma turma do instrutor autenticado.
     *
     * @param classroomId ID da turma
     * @param dto         DTO contendo o ID do vínculo a ser removido
     * @throws NotFoundException caso turma ou estudante não sejam encontrados
     */
    public void removeStudentFromClass(Long classroomId, OnlyIdDTO dto){
        User instructor = authService.getAuthenticatedUser();

        classroomRepository.findOneByIdAndInstructorId(classroomId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        StudentClassroom studentClassroom = studentClassroomRepository.findByIdAndClassroom_Id(dto.getId(), classroomId)
            .orElseThrow(() -> new NotFoundException("Estudante não encontrado na turma"));

        studentClassroomRepository.delete(studentClassroom);
    }

    /**
     * Lista as turmas ativas associadas ao professor autenticado
     * @return Lista de turmas ativas
     */
    public List<ClassroomDTO> getAvailableClasses(){
        User instructor = authService.getAuthenticatedUser();
        return classroomRepository.findActiveClassroomsByInstructorId(instructor.getId(), true);
    }

    /**
     * Vincula múltiplos cursos à turma do instrutor autenticado.
     * <p>
     * Valida a existência da turma e a propriedade pelo instrutor logado.
     * Os cursos informados são filtrados para considerar apenas os pertencentes ao instrutor.
     * Cursos já vinculados são ignorados.
     * </p>
     *
     * @param classId   ID da turma que receberá os cursos
     * @param courseIds IDs dos cursos a serem vinculados
     * @throws NotFoundException caso a turma não exista ou não pertença ao instrutor
     */
    public void attachCoursesToClass(Long classId, OnlyIdsDTO courseIds) {
        User instructor = authService.getAuthenticatedUser();

        Classroom classroom = classroomRepository.findOneByIdAndInstructorId(classId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        List<Course> courses = courseRepository.findByIdInAndInstructorId(
            courseIds.getIds(),
            instructor.getId()
        );

        courses.forEach(course -> {
            if (!course.getClassrooms().contains(classroom)) {
                course.getClassrooms().add(classroom);
            }
        });
        courseRepository.saveAll(courses);
    }

    /**
     * Desvincula um curso de uma turma do instrutor autenticado.
     * <p>
     * Valida a existência do curso e da turma e a propriedade pelo instrutor logado
     * antes de realizar a operação.
     * </p>
     *
     * @param classId  ID da turma
     * @param courseId ID do curso a ser desvinculado
     * @throws NotFoundException caso curso ou turma não existam ou não pertençam ao instrutor
     */
    public void detachCourseFromClass(Long classId, Long courseId) {
        User instructor = authService.getAuthenticatedUser();

        Course course = courseRepository.findOneByIdAndInstructorId(courseId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Curso não encontrado"));

        Classroom classroom = classroomRepository.findOneByIdAndInstructorId(classId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        course.getClassrooms().remove(classroom);

        courseRepository.save(course);
    }

    /**
     * Obtém o perfil completo de um aluno em uma turma específica.
     * <p>
     * Valida que a turma pertence ao instrutor autenticado e que o aluno
     * está vinculado à turma. Retorna informações incluindo pontuação,
     * ranking, login streak e último acesso.
     * </p>
     *
     * @param classroomId ID da turma
     * @param studentId   ID do aluno
     * @return perfil do aluno com dados da turma
     * @throws NotFoundException caso turma ou aluno não sejam encontrados ou não pertençam ao instrutor
     */
    public StudentProfileDTO getStudentProfile(Long classroomId, Long studentId) {
        User instructor = authService.getAuthenticatedUser();

        // Valida que a turma pertence ao instrutor
        Classroom classroom = classroomRepository.findOneByIdAndInstructorId(classroomId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        // Busca o vínculo do aluno com a turma (pode ser ativo ou inativo)
        StudentClassroom studentClassroom = studentClassroomRepository
            .findByStudentIdAndClassroomId(studentId, classroomId)
            .orElseThrow(() -> new NotFoundException("Aluno não encontrado nesta turma"));

        User student = studentClassroom.getStudent();

        // Calcula pontuação total
        Integer totalScore = calculateTotalScore(student);

        // Calcula ranking na turma
        Integer rank = calculateRank(student, classroom);

        return StudentProfileDTO.builder()
            .id(student.getId())
            .name(student.getName())
            .email(student.getEmail())
            .enrollment(studentClassroom.getEnrollment())
            .classroomId(classroom.getId())
            .className(classroom.getName())
            .score(totalScore)
            .rank(rank)
            .loginStreak(studentClassroom.getActualLoginStreak())
            .lastAccessAt(studentClassroom.getLastAccessAt())
            .active(studentClassroom.isActive())
            .avatarUrl(student.getAvatarUrl())
            .build();
    }

    private Integer calculateTotalScore(User student) {
        Integer modulePoints = studentModuleProgressRepository.sumPointsEarnedByStudent(student);
        return modulePoints != null ? modulePoints : 0;
    }

    private Integer calculateRank(User student, Classroom classroom) {
        List<StudentClassroom> allStudents = studentClassroomRepository
            .findAll()
            .stream()
            .filter(sc -> sc.getClassroom().getId().equals(classroom.getId()) && sc.isActive())
            .toList();

        List<RankingEntryDTO> ranking = allStudents.stream()
            .map(sc -> {
                User s = sc.getStudent();
                Integer score = calculateTotalScore(s);
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
     * Obtém o demonstrativo completo de alunos de uma turma (para instrutor).
     * Inclui módulo atual, pontuação, ranking, dias seguidos e último acesso.
     *
     * @param classroomId ID da turma
     * @return Lista de alunos com dados do demonstrativo
     * @throws NotFoundException se a turma não for encontrada ou não pertencer ao instrutor
     */
    @Transactional(readOnly = true)
    public List<StudentReportDTO> getClassroomReport(Long classroomId) {
        User instructor = authService.getAuthenticatedUser();
        classroomRepository.findOneByIdAndInstructorId(classroomId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        // Busca todos os alunos ativos da turma
        List<StudentClassroom> allStudents = studentClassroomRepository
            .findAll()
            .stream()
            .filter(sc -> sc.getClassroom().getId().equals(classroomId) && sc.isActive())
            .toList();

        // Busca todos os cursos atribuídos à turma
        List<Course> allCourses = courseRepository.findAll();
        List<Course> courses = allCourses.stream()
            .filter(course -> course.getClassrooms().stream()
                .anyMatch(c -> c.getId().equals(classroomId)))
            .collect(Collectors.toList());

        // Busca todos os módulos dos cursos, ordenados por curso e ordem
        List<Module> allModules = courses.stream()
            .flatMap(course -> {
                List<CourseModule> courseModules = courseModuleRepository.findByCourseId(course.getId());
                return courseModules.stream()
                    .sorted(Comparator.comparing(CourseModule::getOrderIndex))
                    .map(CourseModule::getModule);
            })
            .collect(Collectors.toList());

        // Cria mapa de progresso de módulos por aluno
        java.util.Map<Long, java.util.Map<Long, StudentModuleProgress>> moduleProgressByStudent =
            allStudents.stream()
                .collect(Collectors.toMap(
                    sc -> sc.getStudent().getId(),
                    sc -> {
                        List<StudentModuleProgress> progressList = studentModuleProgressRepository
                            .findByStudent(sc.getStudent());
                        return progressList.stream()
                            .collect(Collectors.toMap(
                                p -> p.getModule().getId(),
                                p -> p
                            ));
                    }
                ));

        // Cria mapa de progresso de aulas por aluno
        java.util.Map<Long, java.util.Map<Long, StudentLessonProgress>> lessonProgressByStudent =
            allStudents.stream()
                .collect(Collectors.toMap(
                    sc -> sc.getStudent().getId(),
                    sc -> {
                        List<StudentLessonProgress> progressList = studentLessonProgressRepository
                            .findByStudent(sc.getStudent());
                        return progressList.stream()
                            .collect(Collectors.toMap(
                                p -> p.getLesson().getId(),
                                p -> p
                            ));
                    }
                ));

        // Função para verificar se um módulo está completo
        java.util.function.Function<Module, java.util.function.Function<User, Boolean>> isModuleComplete =
            module -> student -> {
                java.util.Map<Long, StudentModuleProgress> studentModuleProgress =
                    moduleProgressByStudent.getOrDefault(student.getId(), java.util.Collections.emptyMap());
                StudentModuleProgress progress = studentModuleProgress.get(module.getId());

                if (progress == null) {
                    return false;
                }

                // Verifica se todas as aulas foram concluídas
                List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
                java.util.Map<Long, StudentLessonProgress> studentLessonProgress =
                    lessonProgressByStudent.getOrDefault(student.getId(), java.util.Collections.emptyMap());

                boolean allLessonsCompleted = lessons.stream()
                    .allMatch(lesson -> {
                        StudentLessonProgress lessonProgress = studentLessonProgress.get(lesson.getId());
                        return lessonProgress != null && lessonProgress.isCompleted();
                    });

                return allLessonsCompleted && progress.isCompletedQuiz();
            };

        // Função para encontrar o módulo atual de um aluno
        java.util.function.Function<User, String> findCurrentModule = student -> {
            // Encontra o primeiro módulo não completo
            for (Module module : allModules) {
                if (!isModuleComplete.apply(module).apply(student)) {
                    return module.getTitle();
                }
            }
            // Se todos os módulos estão completos, retorna o último
            if (!allModules.isEmpty()) {
                return allModules.get(allModules.size() - 1).getTitle();
            }
            return "N/A";
        };

        // Cria a lista de demonstrativo
        List<StudentReportDTO> report = allStudents.stream()
            .map(sc -> {
                User student = sc.getStudent();
                Integer score = calculateTotalScore(student);
                String currentModule = findCurrentModule.apply(student);

                return StudentReportDTO.builder()
                    .studentId(student.getId())
                    .studentName(student.getName())
                    .currentModule(currentModule)
                    .score(score)
                    .rank(0)
                    .loginStreak(sc.getActualLoginStreak())
                    .lastAccessAt(sc.getLastAccessAt())
                    .build();
            })
            .sorted(Comparator.comparing(StudentReportDTO::getScore).reversed()
                .thenComparing(StudentReportDTO::getStudentName))
            .collect(Collectors.toList());

        // Atribui as posições no ranking
        for (int i = 0; i < report.size(); i++) {
            report.get(i).setRank(i + 1);
        }

        return report;
    }
}
