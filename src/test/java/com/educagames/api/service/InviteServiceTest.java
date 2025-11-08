package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.ConflictException;
import com.educagames.api.exception.ForbiddenException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.invite.CreateInviteRequestDTO;
import com.educagames.api.model.dto.invite.InviteDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.Invite;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.ClassroomRepository;
import com.educagames.api.repository.InviteRepository;
import com.educagames.api.service.email.InviteEmailTemplate;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock
    private InviteRepository inviteRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private InviteEmailTemplate inviteEmailTemplate;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private InviteService inviteService;

    private User adminUser;
    private User instructorUser;
    private Classroom testClassroom;
    private CreateInviteRequestDTO instructorInviteRequest;
    private CreateInviteRequestDTO studentInviteRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inviteService, "expirationHours", 24);
        ReflectionTestUtils.setField(inviteService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(inviteService, "logoUrl", "http://localhost:3000/logo.png");

        adminUser = User.builder()
            .email("admin@test.com")
            .role(Role.ADMIN)
            .active(true)
            .build();
        adminUser.setId(1L);

        instructorUser = User.builder()
            .email("instructor@test.com")
            .role(Role.INSTRUCTOR)
            .active(true)
            .build();
        instructorUser.setId(2L);

        testClassroom = Classroom.builder()
            .name("Turma Test")
            .instructor(instructorUser)
            .active(true)
            .build();
        testClassroom.setId(1L);

        instructorInviteRequest = new CreateInviteRequestDTO("newinstructor@test.com");
        studentInviteRequest = new CreateInviteRequestDTO("student@test.com");
    }

    @Test
    @DisplayName("Deve criar convite de INSTRUCTOR quando ADMIN envia")
    void whenAdminCreatesInstructorInvite_shouldCreateInvite() {
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        when(authService.getAuthenticatedUserDetails()).thenReturn(userDetails);
        when(inviteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(inviteRepository.save(any(Invite.class))).thenAnswer(invocation -> {
            Invite invite = invocation.getArgument(0);
            if (invite.getToken() == null) {
                invite.setToken("test-token");
            }
            return invite;
        });

        when(emailService.send(anyString(), any())).thenReturn(true);

        inviteService.createInvite(instructorInviteRequest, null);

        verify(inviteRepository, times(2)).save(any(Invite.class)); // Uma vez para criar, outra para atualizar status
        verify(emailService).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve criar convite de STUDENT quando INSTRUCTOR envia")
    void whenInstructorCreatesStudentInvite_shouldCreateInvite() {
        CustomUserDetails userDetails = new CustomUserDetails(instructorUser);
        when(authService.getAuthenticatedUserDetails()).thenReturn(userDetails);
        when(classroomRepository.findOneByIdAndInstructorId(1L, 2L)).thenReturn(Optional.of(testClassroom));
        when(inviteRepository.findByClassroomIdAndEmail(eq(1L), eq("student@test.com"))).thenReturn(Optional.empty());
        when(inviteRepository.save(any(Invite.class))).thenAnswer(invocation -> {
            Invite invite = invocation.getArgument(0);
            if (invite.getToken() == null) {
                invite.setToken("test-token");
            }
            return invite;
        });
        when(inviteEmailTemplate.withData(anyString(), anyString(), anyInt(), nullable(Role.class), nullable(String.class)))
            .thenReturn(inviteEmailTemplate);
        when(emailService.send(anyString(), any())).thenReturn(true);

        inviteService.createInvite(studentInviteRequest, 1L);

        verify(inviteRepository, times(2)).save(any(Invite.class)); // Uma vez para criar, outra para atualizar status
        verify(emailService).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando já existe convite para o email")
    void whenInviteAlreadyExists_shouldThrowConflictException() {
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        when(authService.getAuthenticatedUserDetails()).thenReturn(userDetails);
        Invite existingInvite = Invite.builder().email("newinstructor@test.com").resendCount(0).build();
        when(inviteRepository.findByEmail("newinstructor@test.com")).thenReturn(Optional.of(existingInvite));

        assertThrows(ConflictException.class, () -> inviteService.createInvite(instructorInviteRequest, null));
        verify(inviteRepository, never()).save(any(Invite.class));
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando INSTRUCTOR não informa classroomId")
    void whenInstructorCreatesInviteWithoutClassroomId_shouldThrowBadRequestException() {
        CustomUserDetails userDetails = new CustomUserDetails(instructorUser);
        when(authService.getAuthenticatedUserDetails()).thenReturn(userDetails);

        CreateInviteRequestDTO request = new CreateInviteRequestDTO("test@test.com");
        assertThrows(BadRequestException.class, () -> inviteService.createInvite(request, null));
        verify(inviteRepository, never()).save(any(Invite.class));
    }

    @Test
    @DisplayName("Deve listar convites de INSTRUCTOR quando ADMIN lista")
    void whenAdminListsInvites_shouldReturnInstructorInvites() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);

        Invite invite = Invite.builder()
            .email("instructor@test.com")
            .status(InviteStatus.AWAITING_ACCEPTANCE)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .resendCount(0).build();
        invite.setId(1L);

        Page<Invite> invitePage = new PageImpl<>(List.of(invite));
        Pageable pageable = PageRequest.of(0, 10);

        when(inviteRepository.findInstructorInvites(eq(Role.INSTRUCTOR), eq(null), eq(pageable)))
            .thenReturn(invitePage);

        PageResponseDTO<InviteDTO> result = inviteService.listInvites(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(inviteRepository).findInstructorInvites(eq(Role.INSTRUCTOR), eq(null), eq(pageable));
    }

    @Test
    @DisplayName("Deve ignorar classroomId quando ADMIN lista convites")
    void whenAdminFiltersByClassroom_shouldIgnoreClassroomId() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);

        Invite invite = Invite.builder()
            .email("instructor@test.com")
            .status(InviteStatus.AWAITING_ACCEPTANCE)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .resendCount(0).build();
        invite.setId(1L);

        Page<Invite> invitePage = new PageImpl<>(List.of(invite));
        Pageable pageable = PageRequest.of(0, 10);

        when(inviteRepository.findInstructorInvites(eq(Role.INSTRUCTOR), eq(null), eq(pageable)))
            .thenReturn(invitePage);

        PageResponseDTO<InviteDTO> result = inviteService.listInvites(1L, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(inviteRepository).findInstructorInvites(eq(Role.INSTRUCTOR), eq(null), eq(pageable));
        verify(inviteRepository, never()).findStudentInvites(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando INSTRUCTOR não informa classroomId")
    void whenInstructorListsWithoutClassroomId_shouldThrowBadRequestException() {
        when(authService.getAuthenticatedUser()).thenReturn(instructorUser);
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(BadRequestException.class, () -> inviteService.listInvites(null, null, pageable));
    }

    @Test
    @DisplayName("Deve excluir convite quando ADMIN exclui")
    void whenAdminDeletesInvite_shouldDeleteInvite() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(inviteRepository.existsByIdAndSenderId(eq(1L), eq(1L))).thenReturn(true);

        inviteService.deleteInvite(1L);

        verify(inviteRepository).deleteById(eq(1L));
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando tentar excluir convite que não pertence ao usuário")
    void whenDeleteInviteNotOwned_shouldThrowForbiddenException() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(inviteRepository.existsByIdAndSenderId(eq(1L), eq(1L))).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> inviteService.deleteInvite(1L));
        verify(inviteRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve reenviar convite quando não foi reenviado há menos de 2 horas")
    void whenResendInviteNotRecentlyResent_shouldResendInvite() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(inviteRepository.existsByIdAndSenderId(eq(1L), eq(1L))).thenReturn(true);

        Invite invite = Invite.builder()
            .email("test@test.com")
            .token("test-token")
            .lastResendAt(LocalDateTime.now().minusHours(3))
            .resendCount(1)
            .build();
        invite.setId(1L);

        when(inviteRepository.findById(eq(1L))).thenReturn(Optional.of(invite));
        when(inviteRepository.save(any(Invite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(emailService.send(anyString(), any())).thenReturn(true);

        inviteService.resendInvite(1L);

        verify(inviteRepository, times(2)).save(any(Invite.class)); // Uma vez para atualizar timestamp, outra para status
        verify(emailService).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando tentar reenviar convite há menos de 2 horas")
    void whenResendInviteRecentlyResent_shouldThrowBadRequestException() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(inviteRepository.existsByIdAndSenderId(eq(1L), eq(1L))).thenReturn(true);

        Invite invite = Invite.builder()
            .email("test@test.com")
            .token("test-token")
            .lastResendAt(LocalDateTime.now().minusHours(1))
            .resendCount(1)
            .build();
        invite.setId(1L);

        when(inviteRepository.findById(eq(1L))).thenReturn(Optional.of(invite));

        assertThrows(BadRequestException.class, () -> inviteService.resendInvite(1L));
        verify(emailService, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando convite não existe ao reenviar")
    void whenResendInviteNotFound_shouldThrowNotFoundException() {
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(inviteRepository.existsByIdAndSenderId(eq(1L), eq(1L))).thenReturn(true);
        when(inviteRepository.findById(eq(1L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> inviteService.resendInvite(1L));
    }
}
