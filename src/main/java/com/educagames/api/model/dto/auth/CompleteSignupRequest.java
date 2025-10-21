package com.educagames.api.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "Nome do usuário a ser criado", example = "Ana Silva Ribeiro", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O nome não pode ser vazio")
    @Size(min = 3, max = 120, message = "O nome deve ter pelo menos 3 caracteres")
    private final String name;

    @Schema(description = "Senha do usuário a ser criado", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "A senha não pode ser vazia")
    @Size(min = 8, max = 128, message = "A senha deve ter pelo menos 8 caracteres")
    private final String password;
}
