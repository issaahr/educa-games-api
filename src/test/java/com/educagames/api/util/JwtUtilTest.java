package com.educagames.api.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.educagames.api.exception.JwtExpiredException;
import com.educagames.api.exception.JwtInvalidException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "minha-chave-secreta-super-segura-para-testes-jwt-com-256-bits-minimo";
    private static final Long TEST_EXPIRATION = 3600000L;
    private static final Long TEST_USER_ID = 123L;
    private static final String TEST_ROLE = "INSTRUCTOR";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Injeta as propriedades usando ReflectionTestUtils para simular @Value
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido com userId e role")
    void whenGenerateToken_shouldReturnValidJwtToken() {
        // Act
        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_ROLE);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verifica se o token tem a estrutura JWT
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length, "Token JWT deve ter 3 partes separadas por ponto");

        assertDoesNotThrow(() -> {
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .build()
                .parseSignedClaims(token);
        });
    }

    @Test
    @DisplayName("Deve extrair userId correto do token gerado")
    void whenGetUserIdFromGeneratedToken_shouldReturnCorrectUserId() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_ROLE);

        // Act
        Long extractedUserId = jwtUtil.getUserId(token);

        // Assert
        assertEquals(TEST_USER_ID, extractedUserId);
    }

    @Test
    @DisplayName("Deve extrair role correto do token gerado")
    void whenGetRoleFromGeneratedToken_shouldReturnCorrectRole() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_ROLE);

        // Act
        String extractedRole = jwtUtil.getRole(token);

        // Assert
        assertEquals(TEST_ROLE, extractedRole);
    }

    @Test
    @DisplayName("Deve validar token válido sem lançar exceção")
    void whenIsValidWithValidToken_shouldNotThrowException() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_ROLE);

        // Act & Assert
        assertDoesNotThrow(() -> jwtUtil.isValid(token));
    }

    @Test
    @DisplayName("Deve lançar JwtInvalidException para token malformado")
    void whenIsValidWithMalformedToken_shouldThrowJwtInvalidException() {
        // Arrange
        String malformedToken = "token.malformado.aqui";

        // Act & Assert
        JwtInvalidException exception = assertThrows(JwtInvalidException.class,
            () -> jwtUtil.isValid(malformedToken));

        assertEquals("Token de autenticação inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar JwtInvalidException para token com assinatura inválida")
    void whenIsValidWithInvalidSignature_shouldThrowJwtInvalidException() {
        // Arrange
        String tokenWithWrongSignature = Jwts.builder()
            .subject(TEST_USER_ID.toString())
            .claim("role", TEST_ROLE)
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plusMillis(TEST_EXPIRATION)))
            .signWith(Keys.hmacShaKeyFor("chave-diferente-para-gerar-assinatura-invalida-nos-testes".getBytes()))
            .compact();

        // Act & Assert
        JwtInvalidException exception = assertThrows(JwtInvalidException.class,
            () -> jwtUtil.isValid(tokenWithWrongSignature));

        assertEquals("Token de autenticação inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar JwtExpiredException para token expirado")
    void whenIsValidWithExpiredToken_shouldThrowJwtExpiredException() {
        // Arrange
        String expiredToken = Jwts.builder()
            .subject(TEST_USER_ID.toString())
            .claim("role", TEST_ROLE)
            .issuedAt(new Date(System.currentTimeMillis() - 7200000))
            .expiration(new Date(System.currentTimeMillis() - 3600000))
            .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
            .compact();

        // Act & Assert
        JwtExpiredException exception = assertThrows(JwtExpiredException.class,
            () -> jwtUtil.isValid(expiredToken));

        assertEquals("Token de autenticação expirado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar JwtInvalidException quando subject não for um número válido")
    void whenGetUserIdWithInvalidSubject_shouldThrowJwtInvalidException() {
        // Arrange
        String tokenWithInvalidSubject = Jwts.builder()
            .subject("not-a-number")
            .claim("role", TEST_ROLE)
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plusMillis(TEST_EXPIRATION)))
            .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
            .compact();

        // Act & Assert
        JwtInvalidException exception = assertThrows(JwtInvalidException.class,
            () -> jwtUtil.getUserId(tokenWithInvalidSubject));

        assertEquals("Subject do token JWT não é um número válido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar JwtInvalidException para token nulo")
    void whenIsValidWithNullToken_shouldThrowJwtInvalidException() {
        // Act & Assert
        JwtInvalidException exception = assertThrows(JwtInvalidException.class,
            () -> jwtUtil.isValid(null));

        assertEquals("Token de autenticação inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar JwtInvalidException para token vazio")
    void whenIsValidWithEmptyToken_shouldThrowJwtInvalidException() {
        // Act & Assert
        JwtInvalidException exception = assertThrows(JwtInvalidException.class,
            () -> jwtUtil.isValid(""));

        assertEquals("Token de autenticação inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve gerar tokens válidos com diferentes timestamps")
    void whenGenerateTokenMultipleTimes_shouldReturnValidTokens() throws InterruptedException {
        // Act
        String token1 = jwtUtil.generateToken(TEST_USER_ID, TEST_ROLE);

        // Delay para garantir timestamp diferente
        Thread.sleep(100);

        String token2 = jwtUtil.generateToken(TEST_USER_ID, TEST_ROLE);

        // Assert
        assertDoesNotThrow(() -> jwtUtil.isValid(token1));
        assertDoesNotThrow(() -> jwtUtil.isValid(token2));

        // Verifica que ambos contêm os mesmos dados do usuário
        assertEquals(TEST_USER_ID, jwtUtil.getUserId(token1));
        assertEquals(TEST_USER_ID, jwtUtil.getUserId(token2));
        assertEquals(TEST_ROLE, jwtUtil.getRole(token1));
        assertEquals(TEST_ROLE, jwtUtil.getRole(token2));
    }

    @Test
    @DisplayName("Deve gerar token com diferentes userIds")
    void whenGenerateTokenWithDifferentUserIds_shouldReturnDifferentTokens() {
        // Act
        String token1 = jwtUtil.generateToken(123L, TEST_ROLE);
        String token2 = jwtUtil.generateToken(456L, TEST_ROLE);

        // Assert
        assertNotEquals(token1, token2);
        assertEquals(Long.valueOf(123L), jwtUtil.getUserId(token1));
        assertEquals(Long.valueOf(456L), jwtUtil.getUserId(token2));
    }

    @Test
    @DisplayName("Deve gerar token com diferentes roles")
    void whenGenerateTokenWithDifferentRoles_shouldReturnDifferentTokens() {
        // Act
        String instructorToken = jwtUtil.generateToken(TEST_USER_ID, "INSTRUCTOR");
        String adminToken = jwtUtil.generateToken(TEST_USER_ID, "ADMIN");

        // Assert
        assertNotEquals(instructorToken, adminToken);
        assertEquals("INSTRUCTOR", jwtUtil.getRole(instructorToken));
        assertEquals("ADMIN", jwtUtil.getRole(adminToken));
    }
}
