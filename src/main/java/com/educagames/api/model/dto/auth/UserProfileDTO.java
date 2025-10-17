package com.educagames.api.model.dto.auth;

import java.io.Serializable;

import com.educagames.api.model.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO para dados do perfil do usuário autenticado.
 * <p>
 * Contém informações essenciais para personalização da interface do usuário,
 * como ID, nome e papel (role).
 */
@Builder
public record UserProfileDTO(
    @Schema(description = "ID único do usuário.", example = "1")
    Long userId,
    @Schema(description = "Nome do usuário.", example = "Nome do Usuário")
    String name,
    @Schema(description = "Papel (role) do usuário no sistema.", example = "INSTRUCTOR")
    Role role
) implements Serializable {
}
