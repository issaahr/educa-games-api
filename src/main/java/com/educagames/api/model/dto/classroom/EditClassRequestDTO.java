package com.educagames.api.model.dto.classroom;

import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EditClassRequestDTO {

    @Schema(description = "Nome da turma", example = "Turma de Java")
    @Size(min = 2, max = 120, message = "O nome deve ter pelo menos 2 caracteres")
    private String name;

    @Schema(description = "Se a turma está ativa ou não", example = "true")
    private Boolean active;
}
