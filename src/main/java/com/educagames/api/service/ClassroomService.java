package com.educagames.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsResponseDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.classroom.EditClassRequestDTO;
import com.educagames.api.model.dto.classroom.StudentClassroomResponseDTO;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.user.ChangeUserStatusDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.StudentClassroom;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.ClassroomRepository;
import com.educagames.api.repository.StudentClassroomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentClassroomRepository studentClassroomRepository;
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
}
