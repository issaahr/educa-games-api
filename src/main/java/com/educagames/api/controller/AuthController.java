package com.educagames.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.model.dto.auth.CompleteSignupRequest;
import com.educagames.api.model.dto.auth.InviteDetailsResponseDTO;
import com.educagames.api.model.dto.auth.LoginRequestDTO;
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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para gerenciamento de autenticação")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "Valida um convite", description = "Verifica se um token de convite (UUID) é válido e retorna seus detalhes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Convite válido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Convite válido\", \"data\": {\"email\": \"usuario@exemplo.com\", \"role\": \"INSTRUCTOR\"}}"))),
            @ApiResponse(responseCode = "404", description = "Convite inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Convite inválido.\", \"errors\": null}"))),
            @ApiResponse(responseCode = "410", description = "Convite expirado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Link expirado, solicite um novo.\", \"errors\": null}"))),
            @ApiResponse(responseCode = "409", description = "Convite já usado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Convite já utilizado.\", \"errors\": null}")))
    })
    @GetMapping("/validate-invite")
    public ResponseEntity<SuccessResponse<InviteDetailsResponseDTO>> validateInvite(@RequestParam("token") String token) {
        InviteDetailsResponseDTO inviteDetails = authService.validateInvite(token);
        return ResponseUtils.ok(inviteDetails, "Convite válido");
    }

    @Operation(summary = "Autentica um usuário", description = "Valida as credenciais do usuário. Se bem-sucedido, o token JWT é enviado em um cookie HttpOnly seguro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Login bem-sucedido. O token foi definido no cookie HttpOnly."),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"email: O email é obrigatório\"]}"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Email ou senha incorretos\", \"errors\": null}")))
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response
    ) {
        authService.login(request, response);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Realiza o logout do usuário", description = "Invalida o cookie de autenticação do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout bem-sucedido. Nenhuma resposta no corpo")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieUtil.removeAuthCookie(response);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Obtém dados do usuário autenticado", description = "Retorna informações do perfil do usuário logado, como ID, nome e papel (role).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário obtidos com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Dados do usuário obtidos com sucesso\", \"data\": {\"userId\": 1, \"name\": \"Nome do Usuário\", \"role\": \"INSTRUCTOR\"}}"))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Usuário não autenticado\", \"errors\": null}")))
    })
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserProfileDTO>> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileDTO userProfile = authService.getUserProfile(userDetails.getUser().getId());
        return ResponseUtils.ok(userProfile, "Dados do usuário obtidos com sucesso");
    }

    @Operation(summary = "Finaliza o cadastro de um novo usuário a partir de um convite", description = "Valida um token de convite e cria um novo usuário com o nome e senha fornecidos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cadastro finalizado com sucesso.",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Cadastro realizado com sucesso!\", \"data\": null}"))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou convite não encontrado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Validation Error", value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"password: A senha deve ter no mínimo 8 caracteres\"]}"),
                                    @ExampleObject(name = "Invite Not Found", value = "{\"message\": \"Convite inválido ou expirado.\", \"errors\": null}")
                            })),
            @ApiResponse(responseCode = "409", description = "Convite já utilizado ou e-mail já cadastrado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Convite já utilizado.\", \"errors\": null}"))),
            @ApiResponse(responseCode = "410", description = "Convite expirado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Link expirado, solicite um novo.\", \"errors\": null}")))
    })
    @PostMapping("/complete-signup")
    public ResponseEntity<SuccessResponse<Void>> completeSignup(@Valid @RequestBody CompleteSignupRequest request) {
        authService.completeSignup(request);
        return ResponseUtils.created(null, "Cadastro realizado com sucesso!");
    }
}
