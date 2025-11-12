package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsResponseDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
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
}
