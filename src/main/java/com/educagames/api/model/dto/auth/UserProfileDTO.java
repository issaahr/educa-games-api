package com.educagames.api.model.dto.auth;

import java.io.Serializable;

import lombok.Builder;

/**
 * DTO para dados do perfil do usuário autenticado.
 *
 * Contém informações para personalização da interface.
 * O role é obtido no login para decisão de rota.
 */
@Builder
public record UserProfileDTO(
    Long userId,
    String name
) implements Serializable {
}
