package com.educagames.api.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.config.PublicEndpoints;
import com.educagames.api.exception.JwtExpiredException;
import com.educagames.api.exception.JwtInvalidException;
import com.educagames.api.service.CustomUserDetailsService;
import com.educagames.api.util.CookieUtil;
import com.educagames.api.util.JwtUtil;

/**
 * Filtro responsável por validar o token JWT em cada requisição.
 * <p>
 * Ignora endpoints públicos definidos em {@link PublicEndpoints} e autentica
 * o contexto de segurança para rotas protegidas.
 * O token é obtido de um cookie HttpOnly.
 * </p>
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtFilter(JwtUtil jwtUtil, CookieUtil cookieUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Define as rotas públicas que não exigem validação de token.
     * Usa {@link PublicEndpoints#PUBLIC_ENDPOINTS} como fonte centralizada.
     *
     * @param request requisição HTTP atual
     * @return {@code true} se o filtro não deve ser aplicado
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return PublicEndpoints.PUBLIC_ENDPOINTS.stream()
            .anyMatch(p -> pathMatcher.match(p, request.getRequestURI()));
    }

    /**
     * Executa a validação do token JWT presente no cookie.
     * Caso o token seja válido, autentica o contexto de segurança da requisição.
     *
     * @param request  requisição HTTP
     * @param response resposta HTTP
     * @param chain    cadeia de filtros do Spring Security
     */
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String token = cookieUtil.getTokenFromCookie(request.getCookies());

        if (token != null) {
            try {
                jwtUtil.isValid(token);

                Long userId = jwtUtil.getUserId(token);
                CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtExpiredException | JwtInvalidException e) {
                request.setAttribute("exception", e);
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
