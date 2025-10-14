package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.educagames.api.exceptions.UnauthorizedException;
import com.educagames.api.model.dto.auth.AuthResult;
import com.educagames.api.model.dto.auth.LoginRequestDTO;
import com.educagames.api.model.dto.auth.UserProfileDTO;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.UserRepository;
import com.educagames.api.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

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
    @DisplayName("Deve retornar AuthResult ao logar com credenciais válidas")
    void whenLoginWithValidCredentials_shouldReturnAuthResult() {
        String fakeToken = "fake-jwt-token";
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getId(), testUser.getRole().toString())).thenReturn(fakeToken);

        AuthResult result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals(fakeToken, result.token());
        assertEquals(Role.INSTRUCTOR.name(), result.role());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtUtil, times(1)).generateToken(testUser.getId(), testUser.getRole().toString());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao logar com email inexistente")
    void whenLoginWithNonExistentEmail_shouldThrowUnauthorizedException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));

        assertEquals("Email ou senha incorretos", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao logar com senha incorreta")
    void whenLoginWithIncorrectPassword_shouldThrowUnauthorizedException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));

        assertEquals("Email ou senha incorretos", exception.getMessage());

        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Deve retornar UserProfileDTO para um usuário existente e ativo")
    void whenGetUserProfileForActiveUser_shouldReturnUserProfileDTO() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserProfileDTO userProfile = authService.getUserProfile(userId);

        assertNotNull(userProfile);
        assertEquals(testUser.getId(), userProfile.userId());
        assertEquals(testUser.getName(), userProfile.name());

        verify(userRepository, times(1)).findById(userId);
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
}
