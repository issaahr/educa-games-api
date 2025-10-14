package com.educagames.api.model.dto.auth;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO para dados do perfil do usuário autenticado.
 * <p>
 * Contém informações para personalização da interface.
 * O role é obtido no login para decisão de rota.
 */
@Builder
public record UserProfileDTO(
    @Schema(description = "ID único do usuário.", example = "1")
    Long userId,
    @Schema(description = "Nome do usuário.", example = "Nome do Usuário")
    String name
) implements Serializable {
}
