package com.educagames.api.util;

import com.educagames.api.exceptions.JwtExpiredException;
import com.educagames.api.exceptions.JwtInvalidException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Utilitário para geração, validação e extração de dados de tokens JWT.
 * <p>
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
            .expiration(Date.from(Instant.now().plusMillis(expiration)))
            .signWith(getKey())
            .compact();
    }

    /**
     * Valida se um token JWT é válido e não expirado.
     * <p>
     * O método não retorna nada. Se o token for inválido ou expirado,
     * uma exceção específica (JwtExpiredException ou JwtInvalidException) será lançada.
     *
     * @param token token JWT a ser validado
     * @throws JwtExpiredException se o token estiver expirado
     * @throws JwtInvalidException se o token for malformado ou tiver uma assinatura inválida
     */
    public void isValid(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException("Token de autenticação expirado");
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new JwtInvalidException("Token de autenticação inválido");
        }
    }

    /**
     * Extrai o ID do usuário de um token JWT válido.
     *
     * @param token token JWT válido
     * @return ID do usuário extraído do token
     * @throws JwtInvalidException se o "subject" do token não for um número válido.
     */
    public Long getUserId(String token) {
        String subject = Jwts.parser().verifyWith(getKey()).build()
            .parseSignedClaims(token).getPayload().getSubject();
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new JwtInvalidException("Subject do token JWT não é um número válido");
        }
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
