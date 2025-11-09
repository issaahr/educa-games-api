package com.educagames.api.model.dto.user;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProfileResponseDTO {

    @Schema(description = "id do usuário", example = "1")
    private final Long id;

    @Schema(description = "Nome do usuário", example = "John Doe" )
    private final String name;

    @Schema(description = "Email do usuário", example = "email@example.com" )
    private final String email;

    @Schema(description = "url da hospedagem do avatar do usuário")
    private final String avatarUrl;

    @Schema(description = "Data de nascimento do usuário")
    private final LocalDate birthDate;

    @Schema(description = "Biografia do usuário")
    private final String description;

    @Schema(description = "Número de matrícula de estudante")
    private final String enrollment;

}
