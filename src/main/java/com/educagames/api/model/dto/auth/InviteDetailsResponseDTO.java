package com.educagames.api.model.dto.auth;

import com.educagames.api.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InviteDetailsResponseDTO {

    @Schema(description = "Email para preencher o campo na tela de cadastro", example = "email@email.com")
    private String email;

    @Schema(description = "Papel (role) do usuário no sistema.", example = "INSTRUCTOR")
    @JsonProperty("role")
    private Role type;

    @Schema(description = "Nome da turma", example = "Turma de Java")
    private String className;

    @Schema(description = "Indica se é necessário criar um novo usuário (true) ou apenas vincular à turma (false). Para STUDENT, será false se o email já estiver cadastrado.", example = "true")
    private Boolean requiresSignup;

}
