package com.educagames.api.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.educagames.api.config.PublicEndpoints;

/**
 * Filtro responsável por validar a origem das requisições de forma estrita.
 * <p>
 * Comportamento por ambiente:
 * <ul>
 *   <li><b>Desenvolvimento:</b> Não valida origem</li>
 *   <li><b>Produção:</b> Valida origem obrigatoriamente em endpoints sensíveis</li>
 * </ul>
 */
@Component
public class StrictOriginFilter extends OncePerRequestFilter {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final Environment environment;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public StrictOriginFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        boolean isSensitive = PublicEndpoints.SENSITIVE_ENDPOINTS.stream()
            .anyMatch(endpoint -> pathMatcher.match(endpoint, requestUri));

        if (!isSensitive) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isDevelopment = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        if (isDevelopment) {
            filterChain.doFilter(request, response);
            return;
        }

        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        List<String> allowed = Arrays.asList(allowedOrigins.split(","));
        boolean isAllowed = false;

        if (origin != null && !origin.isEmpty()) {
            isAllowed = allowed.stream()
                .anyMatch(allowedOrigin -> origin.equals(allowedOrigin.trim()));
        }
        else if (referer != null && !referer.isEmpty()) {
            isAllowed = allowed.stream()
                .anyMatch(allowedOrigin -> referer.startsWith(allowedOrigin.trim()));
        }

        if (!isAllowed) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"message\": \"Origem não autorizada\", \"errors\": null}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
