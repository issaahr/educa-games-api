package com.educagames.api.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.service.CustomUserDetailsService;
import com.educagames.api.exception.JwtExpiredException;
import com.educagames.api.exception.JwtInvalidException;
import com.educagames.api.util.CookieUtil;
import com.educagames.api.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_URLS = List.of(
        "/api/auth/login",
        "/api/auth/logout",
        "/api/auth/validate-invite",
        "/api/auth/complete-signup",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/health"
    );

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtFilter(JwtUtil jwtUtil, CookieUtil cookieUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return PUBLIC_URLS.stream()
            .anyMatch(p -> pathMatcher.match(p, request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            token = authHeader.substring(BEARER_PREFIX.length());
        }
        if (token == null) {
            token = cookieUtil.getTokenFromCookie(request.getCookies());
        }

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
