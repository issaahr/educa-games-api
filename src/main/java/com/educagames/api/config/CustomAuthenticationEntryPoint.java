package com.educagames.api.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.educagames.api.exceptions.JwtExpiredException;
import com.educagames.api.exceptions.JwtInvalidException;
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.MalformedJwtException;

/**
 * Manipula erros de autenticação JWT, retornando respostas HTTP apropriadas.
 *
 * Este componente intercepta requisições não autenticadas ou com tokens inválidos
 * e retorna uma resposta JSON padronizada com código 401.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Verifica se há uma exceção JWT no request (definida pelo JwtFilter)
        Exception jwtException = (Exception) request.getAttribute("exception");

        String message;
        if (jwtException instanceof JwtExpiredException) {
            message = "Token de autenticação expirado";
        } else if (jwtException instanceof JwtInvalidException || jwtException instanceof MalformedJwtException) {
            message = "Token de autenticação inválido";
        } else {
            message = "Token de autenticação necessário";
        }

        ErrorResponse errorResponse = new ErrorResponse(message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
