package com.educagames.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.classroom.StudentClassroomDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.ClassroomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final AuthService authService;

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
    public List<ClassroomDTO> listClasses (){
        User instructor = authService.getAuthenticatedUser();

        List<Classroom> classes = classroomRepository.findByInstructorId(instructor.getId());

        return classes.stream()
            .map( classroom -> {
                ClassroomDTO dto = new ClassroomDTO();
                dto.setId(classroom.getId());
                dto.setName(classroom.getName());
                dto.setActive(classroom.isActive());
                return dto;
            })
            .toList();
    }

    /**
     * Obtém os detalhes completos de uma turma específica.
     * <p>
     * Verifica se a turma pertence ao instrutor autenticado antes de retornar.
     * Inclui informações sobre os alunos da turma.
     * </p>
     *
     * @param id ID da turma a ser consultada
     * @return detalhes da turma incluindo lista de alunos
     * @throws NotFoundException se a turma não for encontrada ou não pertencer ao instrutor
     */
    public ClassroomDetailsDTO getClassById(Long id){
        User instructor = authService.getAuthenticatedUser();

        Classroom classroom = classroomRepository.findByIdAndInstructorId(id, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));

        ClassroomDetailsDTO dto = new ClassroomDetailsDTO();
        dto.setId(classroom.getId());
        dto.setName(classroom.getName());
        dto.setStudents(getClassStudents(classroom));
        return dto;
    }

    /**
     * Obtém os alunos da turma.
     * <p>
     * Converte a lista de alunos para o DTO.
     * </p>
     *
     * @param classroom Turma a ser consultada
     * @return lista de alunos convertidos para DTO
     */
    private List<StudentClassroomDTO> getClassStudents(Classroom classroom) {
        return classroom.getStudents().stream()
            .map(student -> {
                StudentClassroomDTO dto = new StudentClassroomDTO();
                dto.setId(student.getId());
                dto.setName(student.getStudent().getName());

                dto.setEnrollment(student.getEnrollment());
                dto.setActive(student.isActive());
                return dto;
            })
            .toList();
    }
}
