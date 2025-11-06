package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.ForbiddenException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.user.ListInstructorDTO;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User instructorUser;
    private User inactiveInstructorUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
            .email("admin@test.com")
            .role(Role.ADMIN)
            .active(true)
            .build();
        adminUser.setId(1L);

        instructorUser = User.builder()
            .name("Instructor Test")
            .email("instructor@test.com")
            .role(Role.INSTRUCTOR)
            .active(true)
            .build();
        instructorUser.setId(2L);

        inactiveInstructorUser = User.builder()
            .name("Inactive Instructor")
            .email("inactive@test.com")
            .role(Role.INSTRUCTOR)
            .active(false)
            .build();
        inactiveInstructorUser.setId(3L);
    }

    @Test
    @DisplayName("Deve listar instrutores quando ADMIN lista")
    void whenAdminListsInstructors_shouldReturnInstructors() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);

        Page<User> instructorPage = new PageImpl<>(List.of(instructorUser));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByRoleAndActive(eq(Role.INSTRUCTOR), anyBoolean(), eq(null), eq(pageable)))
            .thenReturn(instructorPage);

        PageResponseDTO<ListInstructorDTO> result = userService.listInstructors(true, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(instructorUser.getId(), result.getContent().get(0).getId());
        assertEquals(instructorUser.getName(), result.getContent().get(0).getName());
        assertEquals(instructorUser.getEmail(), result.getContent().get(0).getEmail());
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando não ADMIN tenta listar instrutores")
    void whenNonAdminListsInstructors_shouldThrowForbiddenException() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        Pageable pageable = PageRequest.of(0, 10);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userService.listInstructors(true, null, pageable));
        assertEquals("Você não tem permissão para acessar este recurso", exception.getMessage());
        verify(userRepository, never()).findByRoleAndActive(any(), anyBoolean(), any(), any());
    }

    @Test
    @DisplayName("Deve excluir instrutor quando ADMIN exclui")
    void whenAdminDeletesInstructor_shouldDeleteInstructor() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(instructorUser));

        userService.deleteInstructor(2L);

        verify(userRepository).deleteById(2L);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando instrutor não existe")
    void whenInstructorNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.deleteInstructor(99L));
        assertEquals("Instrutor não encontrado", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando tentar excluir usuário que não é instrutor")
    void whenDeleteNonInstructor_shouldThrowBadRequestException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(BadRequestException.class, () -> userService.deleteInstructor(1L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve alterar status de instrutor quando ADMIN altera")
    void whenAdminChangesInstructorStatus_shouldUpdateStatus() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(instructorUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changeUserStatus(2L, false);

        verify(userRepository).save(any(User.class));
        assertEquals(false, instructorUser.isActive());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando usuário não existe ao alterar status")
    void whenUserNotFoundForStatusChange_shouldThrowNotFoundException() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.changeUserStatus(99L, false));
        verify(userRepository, never()).save(any());
    }
}

