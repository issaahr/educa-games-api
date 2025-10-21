package com.educagames.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

    @Mock
    private HttpServletResponse response;

    private CookieUtil cookieUtil;

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private static final Integer TEST_MAX_AGE = 3600;
    private static final Boolean TEST_SECURE = true;
    private static final String TEST_SAME_SITE = "Strict";
    private static final String TEST_DOMAIN = "educagames.com";

    @BeforeEach
    void setUp() {
        cookieUtil = new CookieUtil();

        // Injeta as propriedades usando ReflectionTestUtils para simular @Value
        ReflectionTestUtils.setField(cookieUtil, "maxAge", TEST_MAX_AGE);
        ReflectionTestUtils.setField(cookieUtil, "secure", TEST_SECURE);
        ReflectionTestUtils.setField(cookieUtil, "sameSite", TEST_SAME_SITE);
        ReflectionTestUtils.setField(cookieUtil, "domain", TEST_DOMAIN);
    }

    @Test
    @DisplayName("Deve adicionar cookie de autenticação com todas as configurações")
    void whenAddAuthCookie_shouldAddCookieWithCorrectAttributes() {
        // Arrange
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        cookieUtil.addAuthCookie(response, TEST_TOKEN);

        // Assert
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("auth_token=" + TEST_TOKEN));
        assertTrue(cookieHeader.contains("Path=/"));
        assertTrue(cookieHeader.contains("Max-Age=" + TEST_MAX_AGE));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=" + TEST_SAME_SITE));
        assertTrue(cookieHeader.contains("Domain=" + TEST_DOMAIN));
    }

    @Test
    @DisplayName("Deve adicionar cookie sem domínio quando domain está vazio")
    void whenAddAuthCookieWithEmptyDomain_shouldNotIncludeDomain() {
        // Arrange
        ReflectionTestUtils.setField(cookieUtil, "domain", "");
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        cookieUtil.addAuthCookie(response, TEST_TOKEN);

        // Assert
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("auth_token=" + TEST_TOKEN));
        assertFalse(cookieHeader.contains("Domain="));
    }

    @Test
    @DisplayName("Deve adicionar cookie sem domínio quando domain é null")
    void whenAddAuthCookieWithNullDomain_shouldNotIncludeDomain() {
        // Arrange
        ReflectionTestUtils.setField(cookieUtil, "domain", null);
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        cookieUtil.addAuthCookie(response, TEST_TOKEN);

        // Assert
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("auth_token=" + TEST_TOKEN));
        assertFalse(cookieHeader.contains("Domain="));
    }

    @Test
    @DisplayName("Deve remover cookie de autenticação com Max-Age=0")
    void whenRemoveAuthCookie_shouldAddCookieWithZeroMaxAge() {
        // Arrange
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        cookieUtil.removeAuthCookie(response);

        // Assert
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("auth_token="));
        assertTrue(cookieHeader.contains("Path=/"));
        assertTrue(cookieHeader.contains("Max-Age=0"));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=" + TEST_SAME_SITE));
        assertTrue(cookieHeader.contains("Domain=" + TEST_DOMAIN));
    }

    @Test
    @DisplayName("Deve extrair token do cookie quando presente")
    void whenGetTokenFromCookieWithAuthToken_shouldReturnToken() {
        // Arrange
        Cookie authCookie = new Cookie("auth_token", TEST_TOKEN);
        Cookie otherCookie = new Cookie("other_cookie", "other_value");
        Cookie[] cookies = {otherCookie, authCookie};

        // Act
        String result = cookieUtil.getTokenFromCookie(cookies);

        // Assert
        assertEquals(TEST_TOKEN, result);
    }

    @Test
    @DisplayName("Deve retornar null quando cookie de auth não está presente")
    void whenGetTokenFromCookieWithoutAuthToken_shouldReturnNull() {
        // Arrange
        Cookie otherCookie1 = new Cookie("session_id", "session123");
        Cookie otherCookie2 = new Cookie("preferences", "dark_mode");
        Cookie[] cookies = {otherCookie1, otherCookie2};

        // Act
        String result = cookieUtil.getTokenFromCookie(cookies);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve extrair token mesmo quando há múltiplos cookies com nomes similares")
    void whenGetTokenFromCookieWithSimilarNames_shouldReturnCorrectToken() {
        // Arrange
        Cookie similarCookie = new Cookie("auth_token_backup", "backup_token");
        Cookie authCookie = new Cookie("auth_token", TEST_TOKEN);
        Cookie anotherSimilar = new Cookie("auth_token_temp", "temp_token");
        Cookie[] cookies = {similarCookie, authCookie, anotherSimilar};

        // Act
        String result = cookieUtil.getTokenFromCookie(cookies);

        // Assert
        assertEquals(TEST_TOKEN, result);
    }

    @Test
    @DisplayName("Deve funcionar com configurações de segurança diferentes")
    void whenAddAuthCookieWithDifferentSecuritySettings_shouldRespectSettings() {
        // Arrange
        ReflectionTestUtils.setField(cookieUtil, "secure", false);
        ReflectionTestUtils.setField(cookieUtil, "sameSite", "Lax");
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        cookieUtil.addAuthCookie(response, TEST_TOKEN);

        // Assert
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("auth_token=" + TEST_TOKEN));
        assertFalse(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=Lax"));
    }

    @Test
    @DisplayName("Deve extrair valor vazio do cookie quando presente")
    void whenGetTokenFromCookieWithEmptyValue_shouldReturnEmptyString() {
        // Arrange
        Cookie authCookie = new Cookie("auth_token", "");
        Cookie[] cookies = {authCookie};

        // Act
        String result = cookieUtil.getTokenFromCookie(cookies);

        // Assert
        assertEquals("", result);
    }
}
