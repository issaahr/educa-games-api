package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.educagames.api.exception.ConflictException;
import com.educagames.api.exception.InviteExpiredException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.exception.UnauthorizedException;
import com.educagames.api.model.dto.auth.CompleteSignupRequest;
import com.educagames.api.model.dto.auth.InviteDetailsResponseDTO;
import com.educagames.api.model.dto.auth.LoginRequestDTO;
import com.educagames.api.model.dto.auth.UserProfileDTO;
import com.educagames.api.model.entity.Invite;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.InviteRepository;
import com.educagames.api.repository.StudentClassroomRepository;
import com.educagames.api.repository.UserRepository;
import com.educagames.api.util.CookieUtil;
import com.educagames.api.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private InviteRepository inviteRepository;

    @Mock
    private StudentClassroomRepository studentClassroomRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO("test@email.com", "password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail(loginRequest.getEmail());
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.INSTRUCTOR);
        testUser.setActive(true);
    }

    @Test
    @DisplayName("Deve chamar o CookieUtil ao logar com credenciais válidas")
    void whenLoginWithValidCredentials_shouldCallCookieUtil() {
        // Arrange
        String fakeToken = "fake-jwt-token";
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getId(), testUser.getRole().toString())).thenReturn(fakeToken);

        // Act
        authService.login(loginRequest, httpServletResponse);

        // Assert
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtUtil, times(1)).generateToken(testUser.getId(), testUser.getRole().toString());
        verify(cookieUtil, times(1)).addAuthCookie(httpServletResponse, fakeToken);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao logar com email inexistente")
    void whenLoginWithNonExistentEmail_shouldThrowUnauthorizedException() {
        // Arrange
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest, httpServletResponse));

        assertEquals("Email ou senha incorretos", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
        verify(cookieUtil, never()).addAuthCookie(any(HttpServletResponse.class), anyString());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao logar com senha incorreta")
    void whenLoginWithIncorrectPassword_shouldThrowUnauthorizedException() {
        // Arrange
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest, httpServletResponse));

        assertEquals("Email ou senha incorretos", exception.getMessage());

        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
        verify(cookieUtil, never()).addAuthCookie(any(HttpServletResponse.class), anyString());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao logar com usuário inativo")
    void whenLoginWithInactiveUser_shouldThrowUnauthorizedException() {
        // Arrange
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        User inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setEmail(loginRequest.getEmail());
        inactiveUser.setPassword("encodedPassword");
        inactiveUser.setRole(Role.INSTRUCTOR);
        inactiveUser.setActive(false);

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest, httpServletResponse));

        assertEquals("Usuário inativo", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
        verify(cookieUtil, never()).addAuthCookie(any(HttpServletResponse.class), anyString());
    }

    @Test
    @DisplayName("Deve retornar UserProfileDTO com role para um usuário existente e ativo")
    void whenGetUserProfileForActiveUser_shouldReturnUserProfileDTO() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        // Para INSTRUCTOR, o repository de StudentClassroom não é chamado

        // Act
        UserProfileDTO userProfile = authService.getUserProfile(userId);

        // Assert
        assertNotNull(userProfile);
        assertEquals(testUser.getId(), userProfile.getUserId());
        assertEquals(testUser.getRole(), userProfile.getRole());

        verify(userRepository, times(1)).findById(userId);
        verify(studentClassroomRepository, never()).findActiveClassroomIdAndNameByStudentId(anyLong());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao buscar perfil de usuário inexistente")
    void whenGetUserProfileForNonExistentUser_shouldThrowUnauthorizedException() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.getUserProfile(userId));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao buscar perfil de usuário inativo")
    void whenGetUserProfileForInactiveUser_shouldThrowUnauthorizedException() {
        Long userId = 2L;
        User inactiveUser = new User();
        inactiveUser.setId(userId);
        inactiveUser.setActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.getUserProfile(userId));

        assertEquals("Usuário inativo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve validar convite válido e retornar detalhes")
    void whenValidateValidInvite_shouldReturnInviteDetails() {
        String token = "valid-invite-token";
        Invite invite = Invite.builder()
            .email("newuser@test.com")
            .token(token)
            .status(InviteStatus.AWAITING_ACCEPTANCE)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .role(Role.INSTRUCTOR)
            .resendCount(0).build();

        when(inviteRepository.findByToken(token)).thenReturn(Optional.of(invite));

        InviteDetailsResponseDTO result = authService.validateInvite(token);

        assertNotNull(result);
        assertEquals(invite.getEmail(), result.getEmail());
        assertEquals(invite.getRole(), result.getType());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando convite não existe")
    void whenValidateNonExistentInvite_shouldThrowNotFoundException() {
        String token = "invalid-token";
        when(inviteRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.validateInvite(token));
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando convite já foi utilizado")
    void whenValidateUsedInvite_shouldThrowConflictException() {
        String token = "used-token";
        Invite invite = Invite.builder()
            .email("user@test.com")
            .token(token)
            .status(InviteStatus.ACCEPTED)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .role(Role.INSTRUCTOR)
            .resendCount(0).build();

        when(inviteRepository.findByToken(token)).thenReturn(Optional.of(invite));

        assertThrows(ConflictException.class, () -> authService.validateInvite(token));
    }

    @Test
    @DisplayName("Deve lançar InviteExpiredException quando convite expirou")
    void whenValidateExpiredInvite_shouldThrowInviteExpiredException() {
        String token = "expired-token";
        Invite invite = Invite.builder()
            .email("user@test.com")
            .token(token)
            .status(InviteStatus.AWAITING_ACCEPTANCE)
            .expiresAt(LocalDateTime.now().minusHours(1))
            .role(Role.INSTRUCTOR)
            .resendCount(0).build();

        when(inviteRepository.findByToken(token)).thenReturn(Optional.of(invite));
        when(inviteRepository.save(any(Invite.class))).thenReturn(invite);

        assertThrows(InviteExpiredException.class, () -> authService.validateInvite(token));
        verify(inviteRepository).save(any(Invite.class));
    }

    @Test
    @DisplayName("Deve completar cadastro com convite válido")
    void whenCompleteSignupWithValidInvite_shouldCreateUser() {
        String token = "valid-token";
        CompleteSignupRequest request = new CompleteSignupRequest(token, "New User", "password123");

        Invite invite = Invite.builder()
            .email("newuser@test.com")
            .token(token)
            .status(InviteStatus.AWAITING_ACCEPTANCE)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .role(Role.INSTRUCTOR)
            .resendCount(0).build();

        when(inviteRepository.findByToken(token)).thenReturn(Optional.of(invite));
        when(userRepository.findByEmail(invite.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inviteRepository.save(any(Invite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.completeSignup(request);

        verify(userRepository).save(any(User.class));
        verify(inviteRepository).save(any(Invite.class));
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando usuário já existe ao completar cadastro")
    void whenCompleteSignupWithExistingUser_shouldThrowConflictException() {
        String token = "valid-token";
        CompleteSignupRequest request = new CompleteSignupRequest(token, "New User", "password123");

        Invite invite = Invite.builder()
            .email("existing@test.com")
            .token(token)
            .status(InviteStatus.AWAITING_ACCEPTANCE)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .role(Role.INSTRUCTOR)
            .resendCount(0).build();

        when(inviteRepository.findByToken(token)).thenReturn(Optional.of(invite));
        when(userRepository.findByEmail(invite.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(ConflictException.class, () -> authService.completeSignup(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
