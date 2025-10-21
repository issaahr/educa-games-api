package com.educagames.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.educagames.api.exception.JwtExpiredException;
import com.educagames.api.exception.JwtInvalidException;
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
        jwtFilter = new JwtFilter(jwtUtil, cookieUtil);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve permitir acesso a URLs públicas sem filtrar")
    void whenRequestToPublicUrl_shouldNotFilter() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // When
        boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

        // Then
        assertTrue(shouldNotFilter);
    }

    @Test
    @DisplayName("Deve filtrar URLs privadas")
    void whenRequestToPrivateUrl_shouldFilter() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/private/endpoint");

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
    @DisplayName("Deve autenticar usuário com token válido no header Authorization")
    void whenValidTokenInAuthorizationHeader_shouldAuthenticateUser() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        doNothing().when(jwtUtil).isValid(VALID_TOKEN);
        when(jwtUtil.getUserId(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getRole(VALID_TOKEN)).thenReturn(TEST_ROLE);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(TEST_USER_ID, auth.getPrincipal());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + TEST_ROLE)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve autenticar usuário com token válido no cookie quando não há header Authorization")
    void whenValidTokenInCookie_shouldAuthenticateUser() throws ServletException, IOException {
        // Given
        Cookie[] cookies = {new Cookie("authToken", VALID_TOKEN)};
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(cookies);
        when(cookieUtil.getTokenFromCookie(cookies)).thenReturn(VALID_TOKEN);
        doNothing().when(jwtUtil).isValid(VALID_TOKEN);
        when(jwtUtil.getUserId(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getRole(VALID_TOKEN)).thenReturn(TEST_ROLE);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(TEST_USER_ID, auth.getPrincipal());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + TEST_ROLE)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve continuar sem autenticação quando não há token")
    void whenNoToken_shouldContinueWithoutAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).isValid(any());
    }

    @Test
    @DisplayName("Deve continuar sem autenticação quando header Authorization não tem Bearer prefix")
    void whenAuthorizationHeaderWithoutBearerPrefix_shouldContinueWithoutAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic " + VALID_TOKEN);
        when(request.getCookies()).thenReturn(null);

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
        JwtInvalidException jwtException = new JwtInvalidException("Token inválido");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
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
        JwtExpiredException jwtException = new JwtExpiredException("Token expirado");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
        doThrow(jwtException).when(jwtUtil).isValid(INVALID_TOKEN);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(request).setAttribute("exception", jwtException);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve priorizar token do header Authorization sobre cookie")
    void whenTokenInBothHeaderAndCookie_shouldPrioritizeHeader() throws ServletException, IOException {
        // Given
        String headerToken = "header.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + headerToken);
        doNothing().when(jwtUtil).isValid(headerToken);
        when(jwtUtil.getUserId(headerToken)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getRole(headerToken)).thenReturn(TEST_ROLE);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).isValid(headerToken);
        verify(cookieUtil, never()).getTokenFromCookie(any());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(TEST_USER_ID, auth.getPrincipal());
    }

    @Test
    @DisplayName("Deve buscar token no cookie quando header Authorization está vazio")
    void whenEmptyAuthorizationHeader_shouldCheckCookie() throws ServletException, IOException {
        // Given
        Cookie[] cookies = {new Cookie("authToken", VALID_TOKEN)};
        when(request.getHeader("Authorization")).thenReturn("");
        when(request.getCookies()).thenReturn(cookies);
        when(cookieUtil.getTokenFromCookie(cookies)).thenReturn(VALID_TOKEN);
        doNothing().when(jwtUtil).isValid(VALID_TOKEN);
        when(jwtUtil.getUserId(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getRole(VALID_TOKEN)).thenReturn(TEST_ROLE);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(cookieUtil).getTokenFromCookie(cookies);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(TEST_USER_ID, auth.getPrincipal());
    }

    @Test
    @DisplayName("Deve continuar sem autenticação quando cookie não contém token")
    void whenCookieWithoutToken_shouldContinueWithoutAuthentication() throws ServletException, IOException {
        // Given
        Cookie[] cookies = {new Cookie("otherCookie", "value")};
        when(request.getHeader("Authorization")).thenReturn(null);
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
