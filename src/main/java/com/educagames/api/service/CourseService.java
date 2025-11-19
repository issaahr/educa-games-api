package com.educagames.api.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.course.CreateCourseRequestDTO;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.Course;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.ClassroomRepository;
import com.educagames.api.repository.CourseRepository;
import com.educagames.api.repository.projection.CourseSummary;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final ClassroomRepository classroomRepository;
    private final CourseRepository courseRepository;
    private final AuthService authService;

    /**
     *  Cria um curso associado ao professor autenticado
     * @param dto
     * @return id do curso criado
     */
    public Long createCourse(CreateCourseRequestDTO dto) {
        User instructor = authService.getAuthenticatedUser();

        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setInstructor(instructor);

        if (dto.getClassroomId() != null) {
            Classroom classroom = classroomRepository.findOneByIdAndInstructorId(dto.getClassroomId(), instructor.getId())
                .orElseThrow(() -> new NotFoundException("Turma não encontrada"));
            course.getClassrooms().add(classroom);
        }

        courseRepository.save(course);
        return course.getId();
    }

    /**
     * Lista cursos associados ao professor autenticado
     * @return Lista de cursos
     */
    public List<CourseSummary> listCourses() {
        User instructor = authService.getAuthenticatedUser();
        return courseRepository.findByInstructorId(instructor.getId());
    }

    /**
     * Lista cursos do instrutor autenticado filtrando por turma, busca e paginação.
     * <p>
     * O filtro de busca aplica-se ao título/descrição do curso (case-insensitive).
     * Retorna apenas cursos pertencentes ao instrutor logado e vinculados à turma informada.
     * </p>
     *
     * @param classroomId ID da turma para filtrar os cursos
     * @param search      termo de busca (opcional) aplicado ao título/descrição
     * @param pageable    configuração de paginação e ordenação
     * @return página com resumo dos cursos
     */
    public PageResponseDTO<CourseSummary> listCoursesByClass(
        Long classroomId,
        String search,
        Pageable pageable) {

        User instructor = authService.getAuthenticatedUser();

        String searchPattern = (search != null && !search.trim().isEmpty())
            ? "%" + search.toLowerCase() + "%"
            : null;

        Page<CourseSummary> page = courseRepository.findByInstructorIdAndClassroomId(
            instructor.getId(),
            classroomId,
            searchPattern,
            pageable
        );

        return PageResponseDTO.<CourseSummary>builder()
            .content(page.getContent())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .size(page.getSize())
            .number(page.getNumber())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
    }

    /**
     * Deleta um curso associado ao professor autenticado
     * @param dto
     */
    public void deleteCourse(OnlyIdDTO dto) {
        User instructor = authService.getAuthenticatedUser();

        Course course = courseRepository.findOneByIdAndInstructorId(dto.getId(), instructor.getId())
            .orElseThrow(() -> new NotFoundException("Curso não encontrado"));

        courseRepository.delete(course);
    }
}
