package com.educagames.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletResponse;

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

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.model.dto.auth.LoginRequestDTO;
import com.educagames.api.model.dto.auth.UserProfileDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.model.entity.User;
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
    @DisplayName("Deve retornar 204 No Content ao logar com credenciais válidas")
    void whenLoginWithValidCredentials_shouldReturnNoContent() {
        // Arrange
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        doNothing().when(authService).login(any(LoginRequestDTO.class), any(HttpServletResponse.class));

        // Act
        ResponseEntity<Void> response = authController.login(loginRequest, httpServletResponse);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        // Verifica se o serviço foi chamado corretamente
        verify(authService, times(1)).login(loginRequest, httpServletResponse);
    }

    @Test
    @DisplayName("Deve retornar 204 No Content ao realizar logout")
    void whenLogout_shouldReturnNoContent() {
        // Arrange
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

        // Act
        ResponseEntity<Void> response = authController.logout(httpServletResponse);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(cookieUtil, times(1)).removeAuthCookie(httpServletResponse);
    }

    @Test
    @DisplayName("Deve retornar 200 OK e os dados do perfil ao chamar /me com usuário autenticado")
    void whenAuthenticatedUserCallsMe_shouldReturnUserProfile() {
        // Arrange
        Long userId = 1L;
        User testUser = User.builder()
            .name("Test User")
            .email("test@email.com")
            .password("encodedPassword")
            .role(Role.INSTRUCTOR)
            .active(true)
            .build();
        testUser.setId(userId);

        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        UserProfileDTO userProfile = UserProfileDTO.builder()
            .userId(userId)
            .role(Role.INSTRUCTOR)
            .classes(null)
            .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(authService.getUserProfile(userId)).thenReturn(userProfile);

        // Act
        ResponseEntity<SuccessResponse<UserProfileDTO>> response = authController.me(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        SuccessResponse<UserProfileDTO> successResponse = response.getBody();
        assertNotNull(successResponse);
        assertEquals("Dados do usuário obtidos com sucesso", successResponse.getMessage());

        UserProfileDTO profileResponse = successResponse.getData();
        assertNotNull(profileResponse);
        assertEquals(userId, profileResponse.getUserId());
        assertEquals(Role.INSTRUCTOR, profileResponse.getRole());

        verify(authService, times(1)).getUserProfile(userId);
    }

}
