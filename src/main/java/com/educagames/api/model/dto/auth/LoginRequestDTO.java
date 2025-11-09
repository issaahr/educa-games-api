package com.educagames.api.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO para dados de login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO{
    @Schema(description = "Endereço de e-mail do usuário.", example = "usuario@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Email inválido")
    @NotBlank(message = "O email é obrigatório")
    String email;

    @Schema(description = "Senha do usuário.", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "A senha é obrigatória")
    String password;
}
