package com.educagames.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.exception.JwtExpiredException;
import com.educagames.api.exception.JwtInvalidException;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.service.CustomUserDetailsService;
import com.educagames.api.util.CookieUtil;
import com.educagames.api.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";
    private static final Long TEST_USER_ID = 123L;
    private static final String TEST_ROLE = "INSTRUCTOR";

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(jwtUtil, cookieUtil, customUserDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve permitir acesso a URLs públicas sem filtrar")
    void whenRequestToPublicUrl_shouldNotFilter() {
        // Given
        when(request.getRequestURI()).thenReturn("/auth/login");

        // When
        boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

        // Then
        assertTrue(shouldNotFilter);
    }

    @Test
    @DisplayName("Deve filtrar URLs privadas")
    void whenRequestToPrivateUrl_shouldFilter() {
        // Given
        when(request.getRequestURI()).thenReturn("/private/endpoint");

        // When
        boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

        // Then
        assertFalse(shouldNotFilter);
    }

    @Test
    @DisplayName("Deve permitir acesso a Swagger URLs")
    void whenRequestToSwaggerUrl_shouldNotFilter() {
        // Given
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // When
        boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

        // Then
        assertTrue(shouldNotFilter);
    }

    @Test
    @DisplayName("Deve permitir acesso a API docs")
    void whenRequestToApiDocsUrl_shouldNotFilter() {
        // Given
        when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");

        // When
        boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

        // Then
        assertTrue(shouldNotFilter);
    }

    @Test
    @DisplayName("Deve permitir acesso ao health check")
    void whenRequestToHealthUrl_shouldNotFilter() {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // When
        boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

        // Then
        assertTrue(shouldNotFilter);
    }

    @Test
    @DisplayName("Deve autenticar usuário com token válido no cookie")
    void whenValidTokenInCookie_shouldAuthenticateUser() throws ServletException, IOException {
        // Given
        User testUser = User.builder()
            .email("test@email.com")
            .password("encodedPassword")
            .role(Role.INSTRUCTOR)
            .active(true)
            .build();
        testUser.setId(TEST_USER_ID);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        Cookie[] cookies = {new Cookie("auth_token", VALID_TOKEN)};
        when(request.getCookies()).thenReturn(cookies);
        when(cookieUtil.getTokenFromCookie(cookies)).thenReturn(VALID_TOKEN);
        doNothing().when(jwtUtil).isValid(VALID_TOKEN);
        when(jwtUtil.getUserId(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(customUserDetailsService.loadUserById(TEST_USER_ID)).thenReturn(userDetails);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertInstanceOf(CustomUserDetails.class, auth.getPrincipal());
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        assertEquals(TEST_USER_ID, principal.getUser().getId());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + TEST_ROLE)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve continuar sem autenticação quando não há token")
    void whenNoToken_shouldContinueWithoutAuthentication() throws ServletException, IOException {
        // Given
        when(request.getCookies()).thenReturn(null);
        when(cookieUtil.getTokenFromCookie(null)).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).isValid(any());
    }

    @Test
    @DisplayName("Deve limpar contexto de segurança quando token é inválido")
    void whenInvalidToken_shouldClearSecurityContext() throws ServletException, IOException {
        // Given
        Cookie[] cookies = {new Cookie("auth_token", INVALID_TOKEN)};
        when(request.getCookies()).thenReturn(cookies);
        when(cookieUtil.getTokenFromCookie(cookies)).thenReturn(INVALID_TOKEN);
        JwtInvalidException jwtException = new JwtInvalidException("Token inválido");
        doThrow(jwtException).when(jwtUtil).isValid(INVALID_TOKEN);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(request).setAttribute("exception", jwtException);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve limpar contexto de segurança quando token está expirado")
    void whenExpiredToken_shouldClearSecurityContext() throws ServletException, IOException {
        // Given
        Cookie[] cookies = {new Cookie("auth_token", INVALID_TOKEN)};
        when(request.getCookies()).thenReturn(cookies);
        when(cookieUtil.getTokenFromCookie(cookies)).thenReturn(INVALID_TOKEN);
        JwtExpiredException jwtException = new JwtExpiredException("Token expirado");
        doThrow(jwtException).when(jwtUtil).isValid(INVALID_TOKEN);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(request).setAttribute("exception", jwtException);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve continuar sem autenticação quando cookie não contém token")
    void whenCookieWithoutToken_shouldContinueWithoutAuthentication() throws ServletException, IOException {
        // Given
        Cookie[] cookies = {new Cookie("otherCookie", "value")};
        when(request.getCookies()).thenReturn(cookies);
        when(cookieUtil.getTokenFromCookie(cookies)).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).isValid(any());
    }
}
