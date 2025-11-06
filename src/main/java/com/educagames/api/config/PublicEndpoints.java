package com.educagames.api.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;

/**
 * Define endpoints públicos e rotas seguras para controle de acesso.
 * Evita duplicação entre SecurityConfig e filtros personalizados.
 */
@Configuration
public class PublicEndpoints {

    /** Rotas públicas acessíveis sem autenticação */
    public static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/auth/login",
        "/auth/logout",
        "/auth/validate-invite",
        "/auth/complete-signup",
        "/actuator/health"
    );

    /**
     * Rotas que podem ser acessadas sem validação de origem em desenvolvimento.
     * Em produção, essas rotas ainda aceitam requisições sem Origin, mas endpoints
     * sensíveis sempre validam origem independente do ambiente.
     */
    public static final List<String> SAFE_NO_ORIGIN_ENDPOINTS = List.of(
        "/actuator/health",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/auth/login"
    );

    /**
     * Rotas sensíveis que SEMPRE validam origem, independente do ambiente.
     * Essas rotas expõem dados sensíveis e DEVEM ter Origin ou Referer válidos.
     */
    public static final List<String> SENSITIVE_ENDPOINTS = List.of(
        "/auth/validate-invite",
        "/auth/complete-signup"
    );
}
