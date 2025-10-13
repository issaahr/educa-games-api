package com.educagames.api.model.dto.auth;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.*;

/**
 * DTO para dados de login.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO implements Serializable {
    @Email(message = "Email inválido")
    @NotBlank(message = "O email é obrigatório")
    String email;

    @NotBlank(message = "A senha é obrigatória")
    String password;
}
