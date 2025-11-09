package com.educagames.api.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompleteSignupRequest {

    @Schema(description = "Código de convite", example = "UUID randômico", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O convite não pode ser vazio")
    private final String invite;

    @Schema(description = "Nome do usuário a ser criado. Opcional quando o usuário já existe (requiresSignup=false).", example = "Ana Silva Ribeiro", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 3, max = 120, message = "O nome deve ter pelo menos 3 caracteres")
    @Pattern(
        regexp = "^[\\p{L}\\p{M}]+([\\s'\\-][\\p{L}\\p{M}]+)*$",
        message = "Nome não pode conter números ou símbolos especiais"
    )
    @Pattern(
        regexp = "^[\\p{L}\\p{M}]+( [\\p{L}\\p{M}]+)+$",
        message = "Informe nome e sobrenome"
    )
    private final String name;

    @Schema(description = "Senha do usuário a ser criado. Opcional quando o usuário já existe (requiresSignup=false).", example = "senha123", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 8, max = 128, message = "A senha deve ter pelo menos 8 caracteres")
    private final String password;
}
