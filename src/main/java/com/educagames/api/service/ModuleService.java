package com.educagames.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.lesson.LessonRequestDTO;
import com.educagames.api.model.dto.lesson.LessonResponseDTO;
import com.educagames.api.model.dto.module.ModuleRequestDTO;
import com.educagames.api.model.dto.module.ModuleResponseDTO;
import com.educagames.api.model.dto.quiz.QuizDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.entity.Course;
import com.educagames.api.model.entity.CourseModule;
import com.educagames.api.model.entity.Lesson;
import com.educagames.api.model.entity.LessonMaterial;
import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.CourseModuleRepository;
import com.educagames.api.repository.CourseRepository;
import com.educagames.api.repository.ModuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final AuthService authService;
    private final LessonService lessonService;
    private final QuizService quizService;

    /**
     * Lista todos os módulos do instrutor autenticado, opcionalmente filtrados por curso.
     *
     * @param courseId ID do curso para filtrar módulos (opcional, null para listar todos)
     * @return Lista de módulos do instrutor
     */
    @Transactional(readOnly = true)
    public List<ModuleResponseDTO> listModules(Long courseId) {
        User instructor = authService.getAuthenticatedUser();
        List<Module> modules = moduleRepository.findByInstructorAndOptionalCourse(instructor.getId(), courseId);
        return modules.stream()
            .map(m -> mapModuleToResponse(m, courseId))
            .toList();
    }

    /**
     * Lista módulos do instrutor autenticado com paginação, busca e ordenação.
     *
     * @param courseId ID do curso para filtrar módulos (opcional, null para listar todos)
     * @param search Termo de busca para filtrar por título do módulo (opcional)
     * @param pageable Configuração de paginação e ordenação
     * @return Página de módulos com informações de paginação
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<ModuleResponseDTO> listModules(Long courseId, String search, Pageable pageable) {
        User instructor = authService.getAuthenticatedUser();
        String searchPattern = search != null && !search.trim().isEmpty()
            ? "%" + search.toLowerCase() + "%"
            : null;

        Page<Module> modules = moduleRepository.findByInstructorAndOptionalCoursePaged(
            instructor.getId(), courseId, searchPattern, pageable
        );

        List<ModuleResponseDTO> content = modules.getContent().stream()
            .map(m -> mapModuleToResponse(m, courseId))
            .toList();

        return PageResponseDTO.<ModuleResponseDTO>builder()
            .content(content)
            .totalElements(modules.getTotalElements())
            .totalPages(modules.getTotalPages())
            .size(modules.getSize())
            .number(modules.getNumber())
            .first(modules.isFirst())
            .last(modules.isLast())
            .build();
    }

    /**
     * Obtém os detalhes de um módulo específico do instrutor autenticado.
     *
     * @param id ID do módulo
     * @return DTO com os detalhes do módulo
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     */
    @Transactional(readOnly = true)
    public ModuleResponseDTO getModule(Long id) {
        User instructor = authService.getAuthenticatedUser();
        Module module = moduleRepository.findOneByIdAndInstructor(id, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Módulo não encontrado"));
        return mapModuleToResponse(module, null);
    }

    /**
     * Cria um novo módulo para o instrutor autenticado.
     * Opcionalmente vincula o módulo a um curso e cria aulas e quiz iniciais.
     *
     * @param dto DTO com os dados do módulo a ser criado
     * @return ID do módulo criado
     * @throws NotFoundException se o curso informado não for encontrado ou não pertencer ao instrutor
     */
    @Transactional
    public Long createModule(ModuleRequestDTO dto) {
        User instructor = authService.getAuthenticatedUser();

        Course course = null;
        if (dto.getCourseId() != null) {
            course = courseRepository.findOneByIdAndInstructorId(dto.getCourseId(), instructor.getId())
                .orElseThrow(() -> new NotFoundException("Curso não encontrado"));
        }

        Module module = new Module();
        module.setTitle(dto.getTitle());
        module.setInstructor(instructor);
        moduleRepository.save(module);

        if (course != null) {
            int orderIndex = courseModuleRepository.findByCourseId(course.getId()).size();
            CourseModule link = CourseModule.builder()
                .course(course)
                .module(module)
                .orderIndex(orderIndex)
                .build();
            courseModuleRepository.save(link);
        }

        lessonService.saveLessonsFromDto(module, dto.getLessons());
        quizService.saveQuizFromDto(module, dto.getQuiz());

        return module.getId();
    }

    /**
     * Atualiza um módulo existente do instrutor autenticado.
     * Atualiza título, vínculo com curso, aulas e quiz conforme informado no DTO.
     *
     * @param id ID do módulo a ser atualizado
     * @param dto DTO com os dados atualizados do módulo
     * @throws NotFoundException se o módulo ou curso não forem encontrados ou não pertencerem ao instrutor
     */
    @Transactional
    public void updateModule(Long id, ModuleRequestDTO dto) {
        User instructor = authService.getAuthenticatedUser();
        Module module = moduleRepository.findOneByIdAndInstructor(id, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Módulo não encontrado"));
        module.setTitle(dto.getTitle());
        moduleRepository.save(module);

        List<CourseModule> existingLinks = courseModuleRepository.findByModuleId(module.getId());
        existingLinks.forEach(courseModuleRepository::delete);

        if (dto.getCourseId() != null) {
            Course targetCourse = courseRepository.findOneByIdAndInstructorId(dto.getCourseId(), instructor.getId())
                .orElseThrow(() -> new NotFoundException("Curso não encontrado"));

            int orderIndex = courseModuleRepository.findByCourseId(targetCourse.getId()).size();
            CourseModule newLink = CourseModule.builder()
                .course(targetCourse)
                .module(module)
                .orderIndex(orderIndex)
                .build();
            courseModuleRepository.save(newLink);
        }

        lessonService.updateLessonsFromDto(module, dto.getLessons());

        if (dto.getQuiz() != null) {
            quizService.replaceQuizFromDto(module, dto.getQuiz());
        }
    }

    /**
     * Adiciona novas aulas ao módulo.
     * As aulas são adicionadas ao final da lista existente.
     *
     * @param moduleId ID do módulo
     * @param lessonDtos Lista de DTOs das aulas a serem adicionadas
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     */
    @Transactional
    public void addLessons(Long moduleId, List<LessonRequestDTO> lessonDtos) {
        Module module = findModuleByIdAndInstructor(moduleId);
        lessonService.addLessonsToModule(module, lessonDtos);
    }

    /**
     * Adiciona novas aulas ao módulo com arquivos de materiais.
     * As aulas são adicionadas ao final da lista existente e os arquivos são aplicados aos materiais recém-criados.
     *
     * @param moduleId ID do módulo
     * @param lessonDtos Lista de DTOs das aulas a serem adicionadas
     * @param files Lista de arquivos para os materiais das aulas (opcional)
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     * @throws BadRequestException se algum arquivo tiver tipo não permitido
     */
    @Transactional
    public void addLessons(Long moduleId, List<LessonRequestDTO> lessonDtos, List<MultipartFile> files) {
        Module module = findModuleByIdAndInstructor(moduleId);
        List<Lesson> before = lessonService.getLessonsByModule(module);
        int startIndex = before.size();

        validateFiles(files);
        addLessons(moduleId, lessonDtos);

        if (files != null && !files.isEmpty()) {
            List<Lesson> newLessons = lessonService.getLessonsByModuleWithMinOrderIndex(module, startIndex);
            applyFilesToMaterials(moduleId, files, newLessons);
        }
    }

    /**
     * Atualiza as aulas do módulo.
     * Aulas com ID são atualizadas, sem ID são criadas, e aulas existentes não presentes na lista são removidas.
     *
     * @param moduleId ID do módulo
     * @param lessonDtos Lista completa de DTOs das aulas (substitui todas as aulas existentes)
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     */
    @Transactional
    public void updateLessons(Long moduleId, List<LessonRequestDTO> lessonDtos) {
        Module module = findModuleByIdAndInstructor(moduleId);
        lessonService.updateLessonsFromDto(module, lessonDtos);
    }

    /**
     * Atualiza as aulas do módulo com arquivos de materiais.
     * Aulas com ID são atualizadas, sem ID são criadas, e aulas existentes não presentes na lista são removidas.
     * Os arquivos são aplicados aos materiais das aulas que não possuem URL.
     *
     * @param moduleId ID do módulo
     * @param lessonDtos Lista completa de DTOs das aulas (substitui todas as aulas existentes)
     * @param files Lista de arquivos para os materiais das aulas (opcional)
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     * @throws BadRequestException se algum arquivo tiver tipo não permitido
     */
    @Transactional
    public void updateLessons(Long moduleId, List<LessonRequestDTO> lessonDtos, List<MultipartFile> files) {
        Module module = findModuleByIdAndInstructor(moduleId);
        validateFiles(files);
        lessonService.updateLessonsFromDto(module, lessonDtos);

        if (files != null && !files.isEmpty()) {
            List<Lesson> lessons = lessonService.getLessonsByModule(module);
            applyFilesToMaterials(moduleId, files, lessons);
        }
    }

    /**
     * Busca um módulo por ID garantindo que pertença ao instrutor autenticado.
     *
     * @param moduleId ID do módulo
     * @return Módulo encontrado
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     */
    private Module findModuleByIdAndInstructor(Long moduleId) {
        User instructor = authService.getAuthenticatedUser();
        return moduleRepository.findOneByIdAndInstructor(moduleId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Módulo não encontrado"));
    }

    /**
     * Obtém o ID do curso vinculado ao módulo.
     * Se courseId for fornecido, retorna ele. Caso contrário, retorna o primeiro curso vinculado ao módulo.
     *
     * @param module Módulo
     * @param courseId ID do curso fornecido (opcional)
     * @return ID do curso vinculado ou null se não houver vínculo
     */
    private Long getLinkedCourseId(Module module, Long courseId) {
        if (courseId != null) return courseId;
        if (module.getCourseLinks() != null && !module.getCourseLinks().isEmpty()) {
            return module.getCourseLinks().get(0).getCourse().getId();
        }
        return null;
    }

    /**
     * Converte uma entidade Module para ModuleResponseDTO.
     *
     * @param module Entidade do módulo
     * @param courseId ID do curso para contexto (opcional)
     * @return DTO de resposta do módulo
     */
    private ModuleResponseDTO mapModuleToResponse(Module module, Long courseId) {
        Long linkedCourseId = getLinkedCourseId(module, courseId);
        List<Lesson> lessons = lessonService.getLessonsByModule(module);
        List<LessonResponseDTO> lessonDTOs = lessonService.mapLessonsToResponse(lessons);
        QuizDTO quizDTO = quizService.getQuizByModule(module);

        return ModuleResponseDTO.builder()
            .id(module.getId())
            .title(module.getTitle())
            .description(null)
            .courseId(linkedCourseId)
            .lessons(lessonDTOs)
            .quiz(quizDTO)
            .build();
    }

    /**
     * Valida os tipos de arquivo permitidos para materiais de aulas.
     * Tipos permitidos: PNG, JPEG, GIF, WebP, PDF ou ZIP.
     *
     * @param files Lista de arquivos a serem validados
     * @throws BadRequestException se algum arquivo tiver tipo não permitido
     */
    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            String ct = file.getContentType();
            if (ct == null || !lessonService.isAllowedLessonContentType(ct)) {
                throw new BadRequestException("Tipo de arquivo não permitido. Tipos aceitos: PNG, JPEG, GIF, WebP, PDF ou ZIP.");
            }
        }
    }

    /**
     * Aplica arquivos aos materiais das aulas que não possuem URL.
     * Os arquivos são associados aos materiais com base no tipo de conteúdo.
     *
     * @param moduleId ID do módulo
     * @param files Lista de arquivos a serem aplicados
     * @param lessons Lista de aulas cujos materiais receberão os arquivos
     */
    private void applyFilesToMaterials(Long moduleId, List<MultipartFile> files, List<Lesson> lessons) {
        List<MultipartFile> queue = new ArrayList<>(files);
        for (Lesson l : lessons) {
            List<LessonMaterial> mats = lessonService.getMaterialsByLesson(l);
            for (LessonMaterial m : mats) {
                if (m.getUrl() != null && !m.getUrl().isBlank()) continue;
                String targetType = lessonService.materialTypeToString(m.getType());
                MultipartFile match = queue.stream()
                    .filter(f -> lessonService.mapContentTypeToMaterialType(f.getContentType()).equals(targetType))
                    .findFirst()
                    .orElse(null);
                if (match != null) {
                    lessonService.updateLessonMaterial(moduleId, l.getId(), m.getId(), match);
                    queue.remove(match);
                    if (queue.isEmpty()) return;
                }
            }
        }
    }

    /**
     * Cria um novo quiz para o módulo.
     *
     * @param moduleId ID do módulo
     * @param quizDto DTO com os dados do quiz a ser criado
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     * @throws BadRequestException se o módulo já possuir um quiz
     */
    @Transactional
    public void createQuiz(Long moduleId, QuizDTO quizDto) {
        Module module = findModuleByIdAndInstructor(moduleId);

        if (quizService.hasQuiz(module)) {
            throw new BadRequestException("O módulo já possui um quiz");
        }

        quizService.saveQuizFromDto(module, quizDto);
    }

    /**
     * Atualiza ou substitui o quiz do módulo.
     * Remove o quiz existente (se houver) e cria um novo com os dados fornecidos.
     *
     * @param moduleId ID do módulo
     * @param quizDto DTO com os dados do quiz atualizado
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     */
    @Transactional
    public void setQuiz(Long moduleId, QuizDTO quizDto) {
        Module module = findModuleByIdAndInstructor(moduleId);
        quizService.replaceQuizFromDto(module, quizDto);
    }

    /**
     * Remove um módulo do instrutor autenticado.
     * Remove também todas as aulas, materiais e quiz associados ao módulo.
     *
     * @param id ID do módulo a ser removido
     * @throws NotFoundException se o módulo não for encontrado ou não pertencer ao instrutor
     */
    @Transactional
    public void deleteModule(Long id) {
        Module module = findModuleByIdAndInstructor(id);
        moduleRepository.delete(module);
    }

}
