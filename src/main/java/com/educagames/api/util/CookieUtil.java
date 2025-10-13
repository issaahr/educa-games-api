package com.educagames.api.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static final String COOKIE_NAME = "auth_token";

    @Value("${cookie.max-age}")
    private Integer maxAge;

    @Value("${cookie.secure}")
    private Boolean secure;

    @Value("${cookie.same-site}")
    private String sameSite;

    @Value("${server.servlet.session.cookie.domain:}")
    private String domain;

    /**
     * Adiciona cookie HttpOnly com token JWT
     * @param response HttpServletResponse
     * @param token String
     */
    public void addAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        if (domain != null && !domain.isBlank()) {
            cookie.setDomain(domain);
        }

        // Define SameSite manualmente no header
        StringBuilder header = new StringBuilder();
        header.append(COOKIE_NAME).append("=").append(token)
            .append("; Path=/; Max-Age=").append(maxAge)
            .append("; HttpOnly; ");
        if (secure) header.append("Secure; ");
        if (domain != null && !domain.isBlank()) header.append("Domain=").append(domain).append("; ");
        header.append("SameSite=").append(sameSite);

        response.addHeader("Set-Cookie", header.toString());
    }

    /**
     * Remove cookie de autenticação (logout)
     * @param response HttpServletResponse
     */
    public void removeAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        if (domain != null && !domain.isBlank()) {
            cookie.setDomain(domain);
        }

        // Define header manualmente também para garantir remoção
        StringBuilder header = new StringBuilder();
        header.append(COOKIE_NAME).append("=")
            .append("; Path=/; Max-Age=0; HttpOnly; ");
        if (secure) header.append("Secure; ");
        if (domain != null && !domain.isBlank()) header.append("Domain=").append(domain).append("; ");
        header.append("SameSite=").append(sameSite);

        response.addHeader("Set-Cookie", header.toString());
    }

    /**
     * Extrai token do cookie
     * @param cookies Cookie[]
     * @return String
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
