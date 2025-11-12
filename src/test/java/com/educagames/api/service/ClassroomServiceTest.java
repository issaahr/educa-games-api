package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsResponseDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.classroom.EditClassRequestDTO;
import com.educagames.api.model.dto.classroom.StudentClassroomResponseDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.user.ChangeUserStatusDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.StudentClassroom;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.ClassroomRepository;
import com.educagames.api.repository.StudentClassroomRepository;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private AuthService authService;

    @Mock
    private StudentClassroomRepository studentClassroomRepository;

    @InjectMocks
    private ClassroomService classroomService;

    private User instructorUser;
    private Classroom testClassroom;
    private CreateClassRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        instructorUser = User.builder()
            .email("instructor@test.com")
            .role(Role.INSTRUCTOR)
            .active(true)
            .build();
        instructorUser.setId(1L);

        testClassroom = Classroom.builder()
            .name("Turma Test")
            .instructor(instructorUser)
            .active(true)
            .students(new ArrayList<>())
            .build();
        testClassroom.setId(1L);

        createRequest = new CreateClassRequestDTO("Nova Turma");
    }

    @Test
    @DisplayName("Deve criar turma quando INSTRUCTOR cria")
    void whenInstructorCreatesClass_shouldCreateClass() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        classroomService.createClass(createRequest);

        verify(classroomRepository).save(any(Classroom.class));
    }

    @Test
    @DisplayName("Deve listar turmas do instrutor autenticado")
    void whenInstructorListsClasses_shouldReturnInstructorClasses() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Classroom> page = new PageImpl<>(java.util.List.of(testClassroom), pageable, 1);

        when(classroomRepository.findByInstructorIdAndActive(1L, true, null, pageable))
            .thenReturn(page);

        PageResponseDTO<ClassroomDTO> result = classroomService.listClasses(true, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testClassroom.getId(), result.getContent().get(0).getId());
        assertEquals(testClassroom.getName(), result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Deve retornar detalhes da turma quando pertence ao instrutor")
    void whenInstructorGetsOwnClass_shouldReturnClassDetails() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        ClassroomDetailsResponseDTO dto = new ClassroomDetailsResponseDTO();
        dto.setId(1L);
        dto.setName(testClassroom.getName());
        dto.setCreatedAt(testClassroom.getCreatedAt());
        dto.setStudentCount(0);
        dto.setCoursesCount(0);
        dto.setActive(testClassroom.isActive());
        when(classroomRepository.findClassroomDetailsByIdAndInstructorId(1L, 1L))
            .thenReturn(Optional.of(dto));

        ClassroomDetailsResponseDTO result = classroomService.getClassById(1L);

        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getName(), result.getName());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando turma não existe")
    void whenClassNotFound_shouldThrowNotFoundException() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findClassroomDetailsByIdAndInstructorId(99L, 1L))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> classroomService.getClassById(99L));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando turma não pertence ao instrutor")
    void whenClassDoesNotBelongToInstructor_shouldThrowNotFoundException() {
        User otherInstructor = User.builder()
            .email("other@test.com")
            .role(Role.INSTRUCTOR)
            .active(true)
            .build();
        otherInstructor.setId(2L);

        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findClassroomDetailsByIdAndInstructorId(1L, 1L))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> classroomService.getClassById(1L));
    }

    @Test
    @DisplayName("Deve excluir turma quando pertence ao instrutor")
    void whenDeleteClassBelongsToInstructor_shouldDelete() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));

        classroomService.deleteClass(1L);

        verify(classroomRepository).delete(eq(testClassroom));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao excluir turma inexistente")
    void whenDeleteClassNotFound_shouldThrowNotFound() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> classroomService.deleteClass(99L));
    }

    @Test
    @DisplayName("Deve editar turma atualizando nome e ativo")
    void whenEditClass_shouldUpdateNameAndActive() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));

        EditClassRequestDTO edit = new EditClassRequestDTO("Nova Turma", false);
        classroomService.editClass(1L, edit);

        verify(classroomRepository).save(eq(testClassroom));
        assertEquals("Nova Turma", testClassroom.getName());
        assertEquals(false, testClassroom.isActive());
    }

    @Test
    @DisplayName("Deve manter valores quando não há mudanças no editClass")
    void whenEditClassWithSameValues_shouldKeepExisting() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));

        EditClassRequestDTO edit = new EditClassRequestDTO("Turma Test", true);
        classroomService.editClass(1L, edit);

        verify(classroomRepository).save(eq(testClassroom));
        assertEquals("Turma Test", testClassroom.getName());
        assertEquals(true, testClassroom.isActive());
    }

    @Test
    @DisplayName("Deve listar alunos da turma com paginação e sem busca")
    void whenListStudents_noSearch_shouldReturnPage() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));

        Pageable pageable = PageRequest.of(0, 10);
        StudentClassroomResponseDTO dto = new StudentClassroomResponseDTO(
            10L, LocalDateTime.now(), "Ana Silva", "ana@test.com", "0000000001", true
        );
        Page<StudentClassroomResponseDTO> page = new PageImpl<>(java.util.List.of(dto), pageable, 1);

        when(studentClassroomRepository.findStudentsByClassroomIdAndActive(1L, true, null, pageable))
            .thenReturn(page);

        PageResponseDTO<StudentClassroomResponseDTO> result = classroomService.listStudentsByClass(1L, true, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(10L, result.getContent().get(0).getId());
        assertEquals("Ana Silva", result.getContent().get(0).getName());
        verify(studentClassroomRepository).findStudentsByClassroomIdAndActive(1L, true, null, pageable);
    }

    @Test
    @DisplayName("Deve aplicar pattern de busca em listagem de alunos")
    void whenListStudents_withSearch_shouldLowercaseAndWildcard() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));

        Pageable pageable = PageRequest.of(0, 10);
        Page<StudentClassroomResponseDTO> page = new PageImpl<>(java.util.List.of(), pageable, 0);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(studentClassroomRepository.findStudentsByClassroomIdAndActive(eq(1L), eq(true), any(String.class), eq(pageable)))
            .thenReturn(page);

        classroomService.listStudentsByClass(1L, true, "Ana", pageable);

        verify(studentClassroomRepository).findStudentsByClassroomIdAndActive(eq(1L), eq(true), captor.capture(), eq(pageable));
        assertEquals("%ana%", captor.getValue());
    }

    @Test
    @DisplayName("Deve atualizar status do estudante quando encontrado na turma")
    void whenUpdateStudentStatus_found_shouldSave() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));

        User student = User.builder().build();
        student.setId(3L);
        StudentClassroom sc = StudentClassroom.builder()
            .active(true)
            .enrollment("0000000001")
            .classroom(testClassroom)
            .student(student)
            .build();
        sc.setId(10L);

        when(studentClassroomRepository.findByIdAndClassroom_Id(10L, 1L)).thenReturn(Optional.of(sc));

        ChangeUserStatusDTO dto = new ChangeUserStatusDTO(10L, false);
        classroomService.updateStudentStatus(dto, 1L);

        verify(studentClassroomRepository).save(eq(sc));
        assertEquals(false, sc.isActive());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando estudante não encontrado ao atualizar status")
    void whenUpdateStudentStatus_notFound_shouldThrow() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));
        when(studentClassroomRepository.findByIdAndClassroom_Id(999L, 1L)).thenReturn(Optional.empty());

        ChangeUserStatusDTO dto = new ChangeUserStatusDTO(999L, true);
        assertThrows(NotFoundException.class, () -> classroomService.updateStudentStatus(dto, 1L));
    }

    @Test
    @DisplayName("Deve remover estudante da turma quando encontrado")
    void whenRemoveStudentFromClass_found_shouldDelete() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));

        User student = User.builder().build();
        student.setId(3L);
        StudentClassroom sc = StudentClassroom.builder()
            .active(true)
            .enrollment("0000000001")
            .classroom(testClassroom)
            .student(student)
            .build();
        sc.setId(10L);

        when(studentClassroomRepository.findByIdAndClassroom_Id(10L, 1L)).thenReturn(Optional.of(sc));

        OnlyIdDTO dto = new OnlyIdDTO();
        ReflectionTestUtils.setField(dto, "id", 10L);

        classroomService.removeStudentFromClass(1L, dto);

        verify(studentClassroomRepository).delete(eq(sc));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao remover estudante inexistente da turma")
    void whenRemoveStudentFromClass_notFound_shouldThrow() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));
        when(studentClassroomRepository.findByIdAndClassroom_Id(999L, 1L)).thenReturn(Optional.empty());

        OnlyIdDTO dto = new OnlyIdDTO();
        ReflectionTestUtils.setField(dto, "id", 999L);

        assertThrows(NotFoundException.class, () -> classroomService.removeStudentFromClass(1L, dto));
    }
}
