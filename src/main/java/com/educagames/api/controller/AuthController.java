package com.educagames.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.AuthService;
import com.educagames.api.util.CookieUtil;
import com.educagames.api.util.ResponseUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller responsável pelos endpoints de autenticação.
 * Gerencia login, logout e informações do usuário autenticado.
 * Utiliza cookies HttpOnly para armazenamento seguro de tokens JWT.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para gerenciamento de autenticação")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    public AuthController(AuthService authService, CookieUtil cookieUtil) {
        this.authService = authService;
        this.cookieUtil = cookieUtil;
    }

    @Operation(summary = "Autentica um usuário", description = "Valida as credenciais do usuário e retorna a role. O token JWT é enviado em um cookie HttpOnly seguro.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login bem-sucedido",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Login realizado com sucesso\", \"data\": {\"role\": \"INSTRUCTOR\"}}"))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"email: O email é obrigatório\", \"password: A senha é obrigatória\"]}"))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Email ou senha incorretos\", \"errors\": null}")))
    })
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

    @Operation(summary = "Realiza o logout do usuário", description = "Invalida o cookie de autenticação do usuário.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logout bem-sucedido. Nenhuma resposta no corpo.")
    })
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(HttpServletResponse response) {
        cookieUtil.removeAuthCookie(response);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Obtém dados do usuário autenticado", description = "Retorna informações do perfil do usuário logado, como ID e nome.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados do usuário obtidos com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Dados do usuário obtidos com sucesso\", \"data\": {\"userId\": 1, \"name\": \"Nome do Usuário\"}}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Usuário não autenticado\", \"errors\": null}")))
    })
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserProfileDTO>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Usuário não autenticado");
        }
        Long userId = (Long) auth.getPrincipal();
        UserProfileDTO userProfile = authService.getUserProfile(userId);
        return ResponseUtils.ok(userProfile, "Dados do usuário obtidos com sucesso");
    }
}
