package com.educagames.api.model.dto.auth;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para seleção de turma pelo aluno")
public class SelectClassRequestDTO {

    @Schema(description = "ID da turma a ser selecionada", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID da turma é obrigatório")
    private Long classroomId;
}
