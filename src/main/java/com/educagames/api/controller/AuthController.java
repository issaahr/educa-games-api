package com.educagames.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.educagames.api.exceptions.UnauthorizedException;
import com.educagames.api.model.dto.auth.AuthResult;
import com.educagames.api.model.dto.auth.LoginRequestDTO;
import com.educagames.api.model.dto.auth.LoginResponseDTO;
import com.educagames.api.model.dto.auth.UserProfileDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.AuthService;
import com.educagames.api.util.CookieUtil;
import com.educagames.api.util.ResponseUtils;

/**
 * Controller responsável pelos endpoints de autenticação.
 *
 * Gerencia login, logout e informações do usuário autenticado.
 * Utiliza cookies HttpOnly para armazenamento seguro de tokens JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * Construtor do AuthController.
     *
     * @param authService serviço de autenticação
     * @param cookieUtil utilitário para manipulação de cookies
     */
    public AuthController(AuthService authService, CookieUtil cookieUtil) {
        this.authService = authService;
        this.cookieUtil = cookieUtil;
    }

    /**
     * Realiza o login do usuário.
     *
     * Valida as credenciais e retorna um token JWT armazenado em cookie HttpOnly.
     *
     * @param request dados de login (email e senha)
     * @param response resposta HTTP para definir o cookie
     * @return resposta com dados do usuário e token em cookie
     */
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponseDTO>> login(
        @Valid @RequestBody LoginRequestDTO request,
        HttpServletResponse response
    ) {
        AuthResult authResult = authService.login(request);

        cookieUtil.addAuthCookie(response, authResult.token());

        LoginResponseDTO loginResponse = new LoginResponseDTO(authResult.role());

        return ResponseUtils.ok(loginResponse, "Login realizado com sucesso");
    }

    /**
     * Realiza o logout do usuário.
     *
     * Remove o cookie de autenticação do navegador.
     *
     * @param response resposta HTTP para remover o cookie
     * @return confirmação de logout
     */
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(HttpServletResponse response) {
        // Usa CookieUtil para remover cookie
        cookieUtil.removeAuthCookie(response);

        return ResponseUtils.ok(null, "Logout realizado com sucesso");
    }

    /**
     * Retorna dados do perfil do usuário autenticado.
     *
     * Busca informações para personalização da interface.
     * O role é obtido no login para decisão de rota.
     *
     * @return dados do perfil do usuário (ID e nome)
     * @throws UnauthorizedException se o usuário não estiver autenticado ou for inativo
     */
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserProfileDTO>> me() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Usuário não autenticado");
        }

        Long userId = (Long) auth.getPrincipal();
        UserProfileDTO userProfile = authService.getUserProfile(userId);

        return ResponseUtils.ok(userProfile, "Dados do usuário obtidos com sucesso");
    }
}
