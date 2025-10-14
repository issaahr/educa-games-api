package com.educagames.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.educagames.api.exceptions.UnauthorizedException;
import com.educagames.api.model.dto.auth.AuthResult;
import com.educagames.api.model.dto.auth.LoginRequestDTO;
import com.educagames.api.model.dto.auth.LoginResponseDTO;
import com.educagames.api.model.dto.auth.UserProfileDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.model.enums.Role;
import com.educagames.api.service.AuthService;
import com.educagames.api.util.CookieUtil;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private CookieUtil cookieUtil;

    @InjectMocks
    private AuthController authController;

    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO("test@email.com", "password123");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve retornar 200 OK e a role do usuário ao logar com credenciais válidas")
    void whenLoginWithValidCredentials_shouldReturnOkAndRole() {
        String fakeToken = "fake-jwt-token";
        AuthResult authResult = new AuthResult(fakeToken, Role.INSTRUCTOR.name());
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(authResult);
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

        ResponseEntity<SuccessResponse<LoginResponseDTO>> response = authController.login(loginRequest, httpServletResponse);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login realizado com sucesso", response.getBody().getMessage());
        assertEquals(Role.INSTRUCTOR, response.getBody().getData().getRole());

        verify(authService, times(1)).login(loginRequest);
        verify(cookieUtil, times(1)).addAuthCookie(httpServletResponse, fakeToken);
    }

    @Test
    @DisplayName("Deve retornar 204 No Content ao realizar logout")
    void whenLogout_shouldReturnNoContent() {
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

        ResponseEntity<SuccessResponse<Void>> response = authController.logout(httpServletResponse);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(cookieUtil, times(1)).removeAuthCookie(httpServletResponse);
    }

    @Test
    @DisplayName("Deve retornar 200 OK e os dados do perfil ao chamar /me com usuário autenticado")
    void whenAuthenticatedUserCallsMe_shouldReturnUserProfile() {
        Long userId = 1L;
        UserProfileDTO userProfile = new UserProfileDTO(userId, "Test User");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(authService.getUserProfile(userId)).thenReturn(userProfile);

        ResponseEntity<SuccessResponse<UserProfileDTO>> response = authController.me();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        SuccessResponse<UserProfileDTO> successResponse = response.getBody();
        assertNotNull(successResponse);
        assertEquals("Dados do usuário obtidos com sucesso", successResponse.getMessage());

        UserProfileDTO profileResponse = successResponse.getData();
        assertNotNull(profileResponse);
        assertEquals(userId, profileResponse.userId());
        assertEquals("Test User", profileResponse.name());

        verify(authService, times(1)).getUserProfile(userId);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao chamar /me sem usuário autenticado")
    void whenUnauthenticatedUserCallsMe_shouldThrowUnauthorizedException() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authController.me());

        assertEquals("Usuário não autenticado", exception.getMessage());

        verify(authService, never()).getUserProfile(anyLong());
    }
}
