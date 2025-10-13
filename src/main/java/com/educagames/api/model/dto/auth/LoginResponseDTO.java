package com.educagames.api.model.dto.auth;

import com.educagames.api.model.enums.Role;

import lombok.*;

/**
 * DTO de resposta de login.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Role role;
}
