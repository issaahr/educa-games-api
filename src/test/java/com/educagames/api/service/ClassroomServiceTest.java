package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.ClassroomRepository;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private AuthService authService;

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
        when(classroomRepository.findByInstructorId(1L)).thenReturn(List.of(testClassroom));

        List<ClassroomDTO> result = classroomService.listClasses();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testClassroom.getId(), result.get(0).getId());
        assertEquals(testClassroom.getName(), result.get(0).getName());
    }

    @Test
    @DisplayName("Deve retornar detalhes da turma quando pertence ao instrutor")
    void whenInstructorGetsOwnClass_shouldReturnClassDetails() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findByIdAndInstructorId(1L, 1L)).thenReturn(Optional.of(testClassroom));

        ClassroomDetailsDTO result = classroomService.getClassById(1L);

        assertNotNull(result);
        assertEquals(testClassroom.getId(), result.getId());
        assertEquals(testClassroom.getName(), result.getName());
        assertNotNull(result.getStudents());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando turma não existe")
    void whenClassNotFound_shouldThrowNotFoundException() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        when(classroomRepository.findByIdAndInstructorId(99L, 1L)).thenReturn(Optional.empty());

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
        when(classroomRepository.findByIdAndInstructorId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> classroomService.getClassById(1L));
    }
}
