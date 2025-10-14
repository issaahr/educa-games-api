package com.educagames.api.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Utilitário para manipulação de cookies de autenticação.
 * Centraliza a criação e remoção de cookies HttpOnly, SameSite e Secure.
 */
@Component
public class CookieUtil {

    private static final String COOKIE_NAME = "auth_token";
    private static final String EMPTY_VALUE = "";

    @Value("${cookie.max-age}")
    private Integer maxAge;

    @Value("${cookie.secure}")
    private Boolean secure;

    @Value("${cookie.same-site}")
    private String sameSite;

    @Value("${server.servlet.session.cookie.domain:}")
    private String domain;

    /**
     * Adiciona o cookie de autenticação HttpOnly à resposta HTTP.
     *
     * @param response HttpServletResponse para adicionar o header.
     * @param token O valor do token JWT a ser armazenado no cookie.
     */
    public void addAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, token);

        builder.path("/");
        builder.maxAge(maxAge);
        builder.httpOnly(true);
        builder.secure(secure);
        builder.sameSite(sameSite);

        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }

        response.addHeader("Set-Cookie", builder.build().toString());
    }

    /**
     * Remove o cookie de autenticação (efetua o logout) ao definir a sua idade máxima como 0.
     *
     * @param response HttpServletResponse para adicionar o header de remoção.
     */
    public void removeAuthCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, EMPTY_VALUE);

        builder.path("/");
        builder.maxAge(0);
        builder.httpOnly(true);
        builder.secure(secure);
        builder.sameSite(sameSite);

        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }

        response.addHeader("Set-Cookie", builder.build().toString());
    }

    /**
     * Extrai o valor do token do array de cookies da requisição.
     *
     * @param cookies O array de cookies da HttpServletRequest.
     * @return O valor do token como String, ou null se não for encontrado.
     */
    public String getTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
