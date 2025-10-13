package com.educagames.api.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utilitário para geração, validação e extração de dados de tokens JWT.
 *
 * Utiliza HMAC-SHA256 para assinatura dos tokens e permite configuração
 * de chave secreta e tempo de expiração via propriedades da aplicação.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Gera um token JWT com userId e role do usuário.
     *
     * @param userId ID único do usuário
     * @param role papel/role do usuário no sistema
     * @return token JWT assinado e pronto para uso
     */
    public String generateToken(Long userId, String role) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getKey())
            .compact();
    }

    /**
     * Valida se um token JWT é válido e não expirado.
     *
     * @param token token JWT a ser validado
     * @return true se o token for válido, false caso contrário
     */
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrai o ID do usuário de um token JWT válido.
     *
     * @param token token JWT válido
     * @return ID do usuário extraído do token
     * @throws NumberFormatException se o subject não for um número válido
     */
    public Long getUserId(String token) {
        String subject = Jwts.parser().verifyWith(getKey()).build()
            .parseSignedClaims(token).getPayload().getSubject();
        return Long.parseLong(subject);
    }

    /**
     * Extrai o role do usuário de um token JWT válido.
     *
     * @param token token JWT válido
     * @return role do usuário extraído do token
     */
    public String getRole(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
            .parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /**
     * Gera a chave secreta para assinatura dos tokens JWT.
     *
     * @return SecretKey configurada com a chave secreta da aplicação
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
